package com.bandyer.video_android_glass_ui

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.video_android_glass_ui.model.Volume
import com.bandyer.video_android_glass_ui.model.*
import com.bandyer.video_android_glass_ui.model.internal.StreamParticipant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        GlassViewModel(ManagersHolder.callManagerInstance!!.get()!!) as T
}

internal class GlassViewModel(private val callManager: CallManager) : ViewModel() {

    val call: Call = callManager.call

    val battery: Flow<Battery> = callManager.battery

    val wifi: Flow<WiFi> = callManager.wifi

    val volume: Volume get() = callManager.getVolume()

    val inCallParticipants: MutableSharedFlow<List<CallParticipant>> =
        MutableSharedFlow<List<CallParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val participants = mutableMapOf<String, CallParticipant>()
            call.participants.forEachParticipant(viewModelScope + CoroutineName("InCallParticipants")) { participant, _, _, state ->
                if (state is CallParticipant.State.Online.InCall) participants[participant.userAlias] =
                    participant
                else participants.remove(participant.userAlias)
                emit(participants.values.toList())
            }.launchIn(viewModelScope)
        }

    val streams: Flow<List<StreamParticipant>> =
        MutableSharedFlow<List<StreamParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val uiStreams = mutableListOf<StreamParticipant>()
            call.participants.forEachParticipant(viewModelScope + CoroutineName("StreamParticipant")) { participant, isLocalPart, streams, state ->
                uiStreams.removeIf { stream -> stream.participant == participant }
                if (isLocalPart || (state is CallParticipant.State.Online.InCall && streams.isNotEmpty()))
                    uiStreams +=
                        if (streams.none { stream -> stream.state !is Stream.State.Closed }) listOf(StreamParticipant(participant, isLocalPart, null))
                        else streams.map { stream -> StreamParticipant(participant, isLocalPart, stream) }
                emit(uiStreams)
            }.launchIn(viewModelScope)
        }

    private val myStreams: Flow<List<Stream>> =
        call.participants.map { it.me }.flatMapLatest { it.streams }

    private val cameraStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.video.firstOrNull { it?.source is Input.Video.Source.Camera } != null } }

    private val audioStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.audio.firstOrNull { it != null } != null } }

    val cameraEnabled: StateFlow<Boolean> =
        MutableStateFlow(false).apply {
            cameraStream
                .filter { it != null }
                .flatMapLatest { it!!.video }
                .filter { it != null }
                .flatMapLatest { it!!.enabled }
                .onEach { value = it }
                .launchIn(viewModelScope)
        }

    val micEnabled: StateFlow<Boolean> =
        MutableStateFlow(false).apply {
            audioStream
                .filter { it != null }
                .flatMapLatest { it!!.audio }
                .filter { it != null }
                .flatMapLatest { it!!.enabled }
                .onEach { value = it }
                .launchIn(viewModelScope)
        }

    private val _micPermission: MutableStateFlow<Permission> = MutableStateFlow(Permission(isAllowed = false, neverAskAgain = false))
    val micPermission: StateFlow<Permission> = _micPermission.asStateFlow()

    private val _camPermission: MutableStateFlow<Permission> = MutableStateFlow(Permission(isAllowed = false, neverAskAgain = false))
    val camPermission: StateFlow<Permission> = _camPermission.asStateFlow()

    private inline fun Flow<CallParticipants>.forEachParticipant(
        scope: CoroutineScope,
        crossinline action: suspend (CallParticipant, Boolean, List<Stream>, Participant.State) -> Unit
    ): Flow<CallParticipants> {
        val pJobs = mutableListOf<Job>()
        return onEach { participants ->
            pJobs.forEach {
                it.cancel()
                it.join()
            }
            pJobs.clear()
            participants.others.plus(participants.me).forEach { participant ->
                pJobs += combine(participant.streams, participant.state) { streams, state ->
                    action(participant, participant == participants.me, streams, state)
                }.launchIn(scope)
            }
        }
    }

    fun requestMicPermission(context: FragmentActivity) {
        viewModelScope.launch { callManager.requestMicPermission(context).also { _micPermission.value = it } }
    }

    fun requestCameraPermission(context: FragmentActivity) {
        viewModelScope.launch { callManager.requestCameraPermission(context).also { _camPermission.value = it } }
    }

    fun enableCamera(enable: Boolean) = callManager.enableCamera(enable)

    fun enableMic(enable: Boolean) = callManager.enableMic(enable)

    fun answer() = callManager.answer()

    fun hangUp() = callManager.hangup()

    fun setVolume(value: Int) = callManager.setVolume(value)
}


