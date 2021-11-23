package com.bandyer.video_android_glass_ui

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.video_android_glass_ui.common.Volume
import com.bandyer.video_android_glass_ui.model.*
import com.bandyer.video_android_glass_ui.model.internal.StreamParticipant
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = GlassViewModel(ManagersHolder.callManagerInstance!!) as T
}

internal class GlassViewModel(private val callManager: CallManager) : ViewModel() {
    val call: Call = callManager.call

    val battery: Flow<Battery> = callManager.battery

    val wifi: Flow<WiFi> = callManager.wifi

    val volume: Volume get() = callManager.getVolume()

    val permissions: Flow<Permissions> = callManager.permissions

    val inCallParticipants: Flow<List<CallParticipant>> =
        MutableSharedFlow<List<CallParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val pJobs = mutableListOf<Job>()
            call.participants.onEach { parts ->
                pJobs.forEach { it.cancel() }
                pJobs.clear()
                val participants = mutableMapOf<String, CallParticipant>()
                parts.others.plus(parts.me).forEach { part ->
                    pJobs += part.state.onEach { state ->
                        if (state is CallParticipant.State.Online.InCall) participants[part.userAlias] = part
                        else participants.remove(part.userAlias)
                        emit(participants.values.toList())
                    }.launchIn(viewModelScope)
                }
            }.launchIn(viewModelScope)
        }

    val streams: Flow<List<StreamParticipant>> =
        MutableSharedFlow<List<StreamParticipant>>(replay = 1, extraBufferCapacity = 1)
            .apply {
                val pJobs = mutableListOf<Job>()
                call.participants.onEach { parts ->
                    pJobs.forEach { it.cancel() }
                    pJobs.clear()
                    val allStreams = mutableListOf<StreamParticipant>()
                    parts.others.plus(parts.me).forEach { part ->
                        pJobs += combine(part.state, part.streams) { state, streams ->
                                    allStreams.removeIf { stream -> stream.participant == part }

                                    if (state is CallParticipant.State.Online.InCall)
                                        allStreams +=
                                            if (streams.none { stream -> stream.state !is Stream.State.Closed }) listOf(StreamParticipant(part, part == parts.me, null))
                                            else streams.map { stream -> StreamParticipant(part, part == parts.me, stream) }

                                    emit(allStreams)
                                }.launchIn(viewModelScope)
                            }
                    }.launchIn(viewModelScope)
            }

    private val myStreams: Flow<List<Stream>> = call.participants.map { it.me }.flatMapLatest { it.streams }

    private val cameraStream: Flow<Stream?> = myStreams.map { streams -> streams.firstOrNull { stream -> stream.video.firstOrNull { it?.source is Input.Video.Source.Camera } != null } }

    private val audioStream: Flow<Stream?> = myStreams.map { streams -> streams.firstOrNull { stream -> stream.audio.firstOrNull { it != null } != null } }

    val cameraEnabled: StateFlow<Boolean> = MutableStateFlow(false).apply {
        cameraStream
            .filter { it != null }
            .flatMapLatest { it!!.video }
            .filter { it != null }
            .flatMapLatest { it!!.enabled }
            .onEach { value = it }
            .launchIn(viewModelScope)
    }

    val micEnabled: StateFlow<Boolean> = MutableStateFlow(false).apply {
        audioStream
            .filter { it != null }
            .flatMapLatest { it!!.audio }
            .filter { it != null }
            .flatMapLatest { it!!.enabled }
            .onEach { value = it }
            .launchIn(viewModelScope)
    }

    fun requestPermissions(context: FragmentActivity) = callManager.requestPermissions(context)

    fun enableCamera(enable: Boolean) = callManager.enableCamera(enable)

    fun enableMic(enable: Boolean) = callManager.enableMic(enable)

    fun answer() = callManager.answer()

    fun hangUp() = callManager.hangup()

    fun setVolume(value: Int) = callManager.setVolume(value)
}


