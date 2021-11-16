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
    val call: Flow<Call> = callManager.call

    val battery: Flow<Battery> = callManager.battery

    val wifi: Flow<WiFi> = callManager.wifi

    val volume: Volume
        get() = callManager.getVolume()

    val callState: Flow<Call.State> = call.flatMapConcat { it.state }

    val participants: Flow<CallParticipants> = call.flatMapConcat { it.participants }

    val inCallParticipants: Flow<List<CallParticipant>> =
        MutableSharedFlow<List<CallParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val jobs = mutableMapOf<String, Job>()
            participants.onEach { participants ->
                val inCallParticipants = mutableMapOf<String, CallParticipant>()
                participants.others.plus(participants.me).forEach { participant ->
                    jobs[participant.userAlias]?.cancel()
                    jobs[participant.userAlias] = participant.state.onEach { state ->
                        if (state is CallParticipant.State.Online.InCall) inCallParticipants[participant.userAlias] = participant
                        else inCallParticipants.remove(participant.userAlias)
                        emit(inCallParticipants.values.toList())
                    }.launchIn(viewModelScope)
                }
            }.launchIn(viewModelScope)
        }

    val recording = call.flatMapConcat { it.isRecording }

    val streams: Flow<List<StreamParticipant>> =
        MutableSharedFlow<List<StreamParticipant>>(replay = 1, extraBufferCapacity = 1)
            .apply {
                val jobs = mutableMapOf<String, Job>()
                participants
                    .onEach { participants ->
                        val allStreams = mutableListOf<StreamParticipant>()
                        participants.others.plus(participants.me)
                            .forEach { participant ->
                                jobs[participant.userAlias]?.cancel()
                                jobs[participant.userAlias] = combine(participant.state, participant.streams) { state, streams ->
                                    allStreams.removeIf { stream -> stream.participant == participant }

                                    if (state is CallParticipant.State.Online.InCall)
                                        allStreams +=
                                            if (streams.none { stream -> stream.state !is Stream.State.Closed }) listOf(StreamParticipant(participant, participant == participants.me, null))
                                            else streams.map { stream -> StreamParticipant(participant, participant == participants.me, stream) }

                                    emit(allStreams)
                                }.launchIn(viewModelScope)
                            }
                    }.launchIn(viewModelScope)
            }

    private val cameraStream: Flow<Stream?> =
        participants
            .map { it.me }
            .flatMapConcat { it.streams }
            .map { streams ->
                streams.firstOrNull { it.video.firstOrNull { video -> video?.source is Input.Video.Source.Camera } != null }
            }

    val cameraEnabled: StateFlow<Boolean> = MutableStateFlow(false).apply {
        cameraStream
            .filter { it != null }
            .flatMapConcat { it!!.video }
            .filter { it != null }
            .flatMapConcat { it!!.enabled }
            .onEach { value = it }
            .launchIn(viewModelScope)
    }

    val micEnabled: StateFlow<Boolean> = MutableStateFlow(false).apply {
        cameraStream
            .filter { it != null }
            .flatMapConcat { it!!.audio }
            .filter { it != null }
            .flatMapConcat { it!!.enabled }
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


