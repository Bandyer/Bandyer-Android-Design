package com.bandyer.video_android_glass_ui

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.collaboration_center.Participant
import com.bandyer.collaboration_center.phonebox.*
import com.bandyer.video_android_glass_ui.model.Permission
import com.bandyer.video_android_glass_ui.model.Volume
import com.bandyer.video_android_glass_ui.model.internal.StreamParticipant
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GlassViewModel(ManagersHolder.callUIDelegateInstance!!.get()!!, ManagersHolder.deviceStatusDelegateInstance!!.get()!!) as T
}

internal class GlassViewModel(private val callUIDelegate: CallUIDelegate, deviceStatusDelegate: DeviceStatusDelegate) : ViewModel() {

    val call: Call = callUIDelegate.call

    val battery: Flow<BatteryInfo> = deviceStatusDelegate.battery

    val wifi: Flow<WiFiInfo> = deviceStatusDelegate.wifi

    val userDetailsWrapper: StateFlow<UserDetailsWrapper> = callUIDelegate.userDetailsWrapper

    val volume: Volume get() = callUIDelegate.getVolume()

    val inCallParticipants: SharedFlow<List<CallParticipant>> =
        MutableSharedFlow<List<CallParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val participants = ConcurrentHashMap<String, CallParticipant>()
            call.participants.forEachParticipant(viewModelScope + CoroutineName("InCallParticipants")) { participant, itsMe, streams, state ->
                if (itsMe || (state == CallParticipant.State.IN_CALL && streams.isNotEmpty())) participants[participant.userAlias] = participant
                else participants.remove(participant.userAlias)
                emit(participants.values.toList())
            }.launchIn(viewModelScope)
        }

    val onParticipantJoin: SharedFlow<CallParticipant> =
        MutableSharedFlow<CallParticipant>().apply {
            var participants = listOf<CallParticipant>()
            inCallParticipants.onEach {
                val diff = it.minus(participants.toSet())
                diff.forEach { p -> emit(p) }
                participants = it
            }.launchIn(viewModelScope)
        }

    val onParticipantLeave: SharedFlow<CallParticipant> =
        MutableSharedFlow<CallParticipant>().apply {
            var participants = listOf<CallParticipant>()
            inCallParticipants.onEach {
                val left = participants.minus(it.toSet())
                left.forEach { p -> emit(p) }
                participants = it
            }.launchIn(viewModelScope)
        }

    val streams: SharedFlow<List<StreamParticipant>> =
        MutableSharedFlow<List<StreamParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val uiStreams = ConcurrentLinkedQueue<StreamParticipant>()
            call.participants.forEachParticipant(viewModelScope + CoroutineName("StreamParticipant")) { participant, itsMe, streams, state ->
                if(itsMe || (state == CallParticipant.State.IN_CALL && streams.isNotEmpty())) {
                    val newStreams = streams.map { StreamParticipant(participant, itsMe, it) }
                    val currentStreams = uiStreams.filter { it.participant == participant }
                    val addedStreams = newStreams - currentStreams.toSet()
                    val removedStreams = currentStreams - newStreams.toSet()
                    uiStreams += addedStreams
                    uiStreams -= removedStreams.toSet()
                } else uiStreams.removeIf { it.participant == participant }
                emit(uiStreams.toList())
            }.launchIn(viewModelScope)
        }

    private val myStreams: Flow<List<Stream>> =
        call.participants.map { it.me }.flatMapLatest { it.streams }

    private val otherStreams: Flow<List<Stream>> =
        call.participants.map { it.others }.flatMapLatest { it.map { it.streams }.merge() }

    private val cameraStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.video.firstOrNull { it is Input.Video.Camera } != null } }

    private val audioStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.audio.firstOrNull { it != null } != null } }

    private val myLiveStreams: StateFlow<List<Stream>> = MutableStateFlow<List<Stream>>(listOf()).apply {
        val liveStreams = ConcurrentLinkedQueue<Stream>()
        val jobs = mutableListOf<Job>()
        myStreams.onEach { streams ->
            jobs.forEach {
                it.cancel()
                it.join()
            }
            jobs.clear()
            liveStreams.clear()
            streams.forEach { stream ->
                jobs += stream.state.onEach {
                    if(it is Stream.State.Live) liveStreams.add(stream)
                    else liveStreams.remove(stream)

                    emit(liveStreams.toList())
                }.launchIn(viewModelScope)
            }
        }.launchIn(viewModelScope)
    }

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

    private val _micPermission: MutableStateFlow<Permission> =
        MutableStateFlow(Permission(isAllowed = false, neverAskAgain = false))
    val micPermission: StateFlow<Permission> = _micPermission.asStateFlow()

    private val _camPermission: MutableStateFlow<Permission> =
        MutableStateFlow(Permission(isAllowed = false, neverAskAgain = false))
    val camPermission: StateFlow<Permission> = _camPermission.asStateFlow()

    val amIAlone: Flow<Boolean> = combine(otherStreams, myLiveStreams, camPermission) { otherStreams, myLiveStreams, camPermission -> !(otherStreams.isNotEmpty() && (myLiveStreams.isNotEmpty() || !camPermission.isAllowed)) }

    val amIVisibleToOthers: Flow<Boolean> = combine(otherStreams, myLiveStreams) { otherStreams, myLiveStreams -> otherStreams.isEmpty() && myLiveStreams.isNotEmpty() }

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
        viewModelScope.launch {
            callUIDelegate.requestMicPermission(context).also { _micPermission.value = it }
        }
    }

    fun requestCameraPermission(context: FragmentActivity) {
        viewModelScope.launch {
            callUIDelegate.requestCameraPermission(context).also { _camPermission.value = it }
        }
    }

    fun enableCamera(enable: Boolean) = callUIDelegate.enableCamera(enable)

    fun enableMic(enable: Boolean) = callUIDelegate.enableMic(enable)

    fun answer() = callUIDelegate.answer()

    fun hangUp() = callUIDelegate.hangup()

    fun setVolume(value: Int) = callUIDelegate.setVolume(value)
}


