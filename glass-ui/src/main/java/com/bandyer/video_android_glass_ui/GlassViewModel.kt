package com.bandyer.video_android_glass_ui

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.collaboration_center.Participant
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.collaboration_center.phonebox.CallParticipant
import com.bandyer.collaboration_center.phonebox.CallParticipants
import com.bandyer.collaboration_center.phonebox.Input
import com.bandyer.collaboration_center.phonebox.Stream
import com.bandyer.video_android_core_ui.CallUIController
import com.bandyer.video_android_core_ui.CallUIDelegate
import com.bandyer.video_android_core_ui.DeviceStatusDelegate
import com.bandyer.video_android_core_ui.UsersDescription
import com.bandyer.video_android_core_ui.model.Permission
import com.bandyer.video_android_core_ui.model.Volume
import com.bandyer.video_android_glass_ui.model.internal.StreamItemData
import com.bandyer.video_android_glass_ui.model.internal.CallStream
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("UNCHECKED_CAST")
internal class GlassViewModelFactory(
    private val callDelegate: CallUIDelegate,
    private val deviceStatusDelegate: DeviceStatusDelegate,
    private val callController: CallUIController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GlassViewModel(callDelegate, deviceStatusDelegate, callController) as T
}

internal class GlassViewModel(
    callDelegate: CallUIDelegate,
    deviceStatusDelegate: DeviceStatusDelegate,
    private val callController: CallUIController
) : ViewModel() {

    val call: SharedFlow<Call> = callDelegate.call

    val currentCall: SharedFlow<Call> =
        MutableSharedFlow<Call>(replay = 1, extraBufferCapacity = 1).apply {
            call
                .take(1)
                .onEach { emit(it) }
                .launchIn(viewModelScope)
        }

    val callState = currentCall.flatMapLatest { it.state }

    val participants = currentCall.flatMapLatest { it.participants }

    val battery: SharedFlow<BatteryInfo> = deviceStatusDelegate.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusDelegate.wifi

    val usersDescription: UsersDescription = callDelegate.usersDescription

    val volume: Volume get() = callController.onGetVolume()

    val inCallParticipants: SharedFlow<List<CallParticipant>> =
        MutableSharedFlow<List<CallParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val participants = ConcurrentHashMap<String, CallParticipant>()
            this@GlassViewModel.participants.forEachParticipant(viewModelScope + CoroutineName("InCallParticipants")) { participant, itsMe, streams, state ->
                if (itsMe || (state == CallParticipant.State.IN_CALL && streams.isNotEmpty()))
                    participants[participant.userAlias] = participant
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

    private val callStreams: SharedFlow<List<CallStream>> =
        MutableSharedFlow<List<CallStream>>(replay = 1, extraBufferCapacity = 1).apply {
            val callStreams = ConcurrentLinkedQueue<CallStream>()
            this@GlassViewModel.participants.forEachParticipant(viewModelScope + CoroutineName("CallStream")) { participant, itsMe, streams, state ->
                if (itsMe || (state == CallParticipant.State.IN_CALL && streams.isNotEmpty())) {
                    val newStreams = streams.map { CallStream(participant, itsMe, it) }
                    val currentStreams = callStreams.filter { it.participant == participant }
                    val addedStreams = newStreams - currentStreams.toSet()
                    val removedStreams = currentStreams - newStreams.toSet()
                    callStreams += addedStreams
                    callStreams -= removedStreams.toSet()
                } else callStreams.removeIf { it.participant == participant }
                emit(callStreams.toList())
            }.launchIn(viewModelScope)
        }

    val streams: SharedFlow<List<StreamItemData>> =
        MutableSharedFlow<List<StreamItemData>>(replay = 1, extraBufferCapacity = 1).apply {
            val uiStreams = ConcurrentHashMap<String, StreamItemData>()
            val pJobs = mutableListOf<Job>()
            callStreams.onEach { streams ->
                pJobs.forEach {
                    it.cancel()
                    it.join()
                }
                pJobs.clear()
                streams.forEach { sp ->
                    val name = usersDescription.name(listOf(sp.participant.userAlias))
                    val image = usersDescription.image(listOf(sp.participant.userAlias))

                    var audioJob: Job? = null
                    var videoJob: Job? = null
                    pJobs += combine(sp.stream.audio, sp.stream.video) { audio, video ->
                        audioJob?.cancel()
                        videoJob?.cancel()
                        audioJob?.join()
                        videoJob?.join()

                        var audioEnabled = false
                        var videoEnabled = false
                        var view: View? = null

                        audio?.also {
                            audioJob = combine(it.state, it.enabled) { s, e ->
                                audioEnabled = if (s !is Input.State.Active) false else e
                                uiStreams[sp.stream.id] = StreamItemData(sp.isMyStream, name, image, audioEnabled, videoEnabled, view)
                                emit(uiStreams.values.toList())
                            }.launchIn(viewModelScope)
                        }

                        video?.also {
                            videoJob = combine(it.state, it.enabled, it.view) { s, e, v ->
                                videoEnabled = if (s !is Input.State.Active) false else e
                                view = v
                                uiStreams[sp.stream.id] = StreamItemData(sp.isMyStream, name, image, audioEnabled, videoEnabled, view)
                                emit(uiStreams.values.toList())
                            }.launchIn(viewModelScope)
                        }
                    }.launchIn(viewModelScope)
                }
            }.launchIn(viewModelScope)
        }

    private val myStreams: Flow<List<Stream>> =
        participants.map { it.me }.flatMapLatest { it.streams }

    private val otherStreams: Flow<List<Stream>> =
        participants.map { it.others }.flatMapLatest { it.map { it.streams }.merge() }

    private val cameraStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.video.firstOrNull { it is Input.Video.Camera } != null } }

    private val audioStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.audio.firstOrNull { it != null } != null } }

    private val myLiveStreams: StateFlow<List<Stream>> =
        MutableStateFlow<List<Stream>>(listOf()).apply {
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
                        if (it is Stream.State.Live) liveStreams.add(stream)
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

    val amIAlone: Flow<Boolean> = combine(
        otherStreams,
        myLiveStreams,
        camPermission
    ) { otherStreams, myLiveStreams, camPermission -> !(otherStreams.isNotEmpty() && (myLiveStreams.isNotEmpty() || !camPermission.isAllowed)) }

    val amIVisibleToOthers: Flow<Boolean> = combine(
        otherStreams,
        myLiveStreams
    ) { otherStreams, myLiveStreams -> otherStreams.isEmpty() && myLiveStreams.isNotEmpty() }

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

    fun onRequestMicPermission(context: FragmentActivity) {
        viewModelScope.launch {
            callController.onRequestMicPermission(context).also { _micPermission.value = it }
        }
    }

    fun onRequestCameraPermission(context: FragmentActivity) {
        viewModelScope.launch {
            callController.onRequestCameraPermission(context).also { _camPermission.value = it }
        }
    }

    fun onEnableCamera(enable: Boolean) = callController.onEnableCamera(enable)

    fun onEnableMic(enable: Boolean) = callController.onEnableMic(enable)

    fun onAnswer() = callController.onAnswer()

    fun onHangup() = callController.onHangup()

    fun onSetVolume(value: Int) = callController.onSetVolume(value)

    fun onSetZoom(value: Int) = callController.onSetZoom(value)
}


