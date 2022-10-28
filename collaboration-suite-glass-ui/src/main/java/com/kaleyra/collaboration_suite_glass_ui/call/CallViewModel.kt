/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.call

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.Participant
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.DeviceStatusObserver
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.call.CallController
import com.kaleyra.collaboration_suite_core_ui.call.CallDelegate
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.model.Volume
import com.kaleyra.collaboration_suite_glass_ui.call.model.StreamParticipant
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@OptIn(ExperimentalCoroutinesApi::class)
internal class CallViewModel : ViewModel() {

    //////////////////////////
    // DeviceStatusObserver //
    //////////////////////////
    private val deviceStatusObserver = DeviceStatusObserver().apply { start() }

    val battery: SharedFlow<BatteryInfo> = deviceStatusObserver.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusObserver.wifi

    override fun onCleared() {
        super.onCleared()
        deviceStatusObserver.stop()
    }

    //////////////////
    // CallDelegate //
    //////////////////
    private var callDelegateScope: CoroutineScope? = null
    var callDelegate: CallDelegate? = null
        set(value) {
            callDelegateScope?.cancel()
            callDelegateScope = CoroutineScope(SupervisorJob(viewModelScope.coroutineContext[Job]))
            value?.call?.onEach { _call.emit(it) }?.launchIn(callDelegateScope!!)
            field = value
        }

    private val _call: MutableSharedFlow<CallUI> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val call: SharedFlow<CallUI> = _call.asSharedFlow()

    val usersDescription: UsersDescription get() = callDelegate?.usersDescription ?: UsersDescription()

    ////////////////////
    // CallController //
    ////////////////////
    private var callControllerScope: CoroutineScope? = null
    var callController: CallController? = null
        set(value) {
            callControllerScope?.cancel()
            callControllerScope = CoroutineScope(SupervisorJob(viewModelScope.coroutineContext[Job]))
            value?.micPermission?.onEach { _micPermission.emit(it) }?.launchIn(callControllerScope!!)
            value?.camPermission?.onEach { _camPermission.emit(it) }?.launchIn(callControllerScope!!)
            field = value
        }

    val volume: Volume get() = callController?.volume ?: Volume(0, 0, 0)

    private val _micPermission: MutableStateFlow<Permission> = MutableStateFlow(Permission(false, false))
    val micPermission: StateFlow<Permission> = _micPermission.asStateFlow()

    private val _camPermission: MutableStateFlow<Permission> = MutableStateFlow(Permission(false, false))
    val camPermission: StateFlow<Permission> = _camPermission.asStateFlow()

    fun onRequestMicPermission(context: FragmentActivity) = callController?.onRequestMicPermission(context)

    fun onRequestCameraPermission(context: FragmentActivity) = callController?.onRequestCameraPermission(context)

    fun onAnswer() = callController?.onAnswer()

    fun onHangup() = callController?.onHangup()

    suspend fun onEnableCamera(context: FragmentActivity, enable: Boolean) = callController?.onEnableCamera(context, enable)

    suspend fun onEnableMic(context: FragmentActivity, enable: Boolean) = callController?.onEnableMic(context, enable)

    fun onSwitchCamera() = callController?.onSwitchCamera()

    fun onSetVolume(value: Int) = callController?.onSetVolume(value)

    fun onSetZoom(value: Float) = callController?.onSetZoom(value)

    ///////////////
    // ViewModel //
    ///////////////
    private var phoneBoxScope: CoroutineScope? = null
    var phoneBox: PhoneBoxUI? = null
        set(value) {
            phoneBoxScope?.cancel()
            phoneBoxScope = CoroutineScope(SupervisorJob(viewModelScope.coroutineContext[Job]))
            value?.state?.onEach { _phoneBoxState.value = it }?.launchIn(phoneBoxScope!!)
            field = value
        }

    private val _phoneBoxState: MutableStateFlow<PhoneBox.State> = MutableStateFlow(PhoneBox.State.Disconnected)
    val phoneBoxState: StateFlow<PhoneBox.State> = _phoneBoxState.asStateFlow()

    val preferredCallType: StateFlow<Call.PreferredType?> =
        call.map { it.extras.preferredType }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val actions: StateFlow<Set<CallUI.Action>> =
        call.flatMapLatest { it.actions }.stateIn(viewModelScope, SharingStarted.Eagerly, setOf())

    val callState: SharedFlow<Call.State> =
        call.flatMapLatest { it.state }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val whiteboard: SharedFlow<Whiteboard> =
        call.mapLatest { it.whiteboard }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val duration: SharedFlow<Long> =
        call.flatMapLatest { it.extras.duration }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val timeToLive: SharedFlow<Long?> = call.flatMapLatest { it.constraints.timeToLive }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val participants: SharedFlow<CallParticipants> =
        call.flatMapLatest { it.participants }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val hasSwitchCamera: StateFlow<Boolean> =
        call
            .flatMapLatest { it.inputs.availableInputs }
            .map { it.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull() }
            .map { c -> c?.lenses?.firstOrNull { it.isRear } != null && c.lenses.firstOrNull { !it.isRear } != null }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val zoom: StateFlow<Input.Video.Camera.Internal.Zoom?> = call
        .flatMapLatest { it.inputs.availableInputs }
        .map { it.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull() }
        .filter { it != null }
        .flatMapLatest { it!!.currentLens }
        .flatMapLatest { it.zoom }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val flashLight: StateFlow<Input.Video.Camera.Internal.FlashLight?> = call
        .flatMapLatest { it.inputs.availableInputs }
        .map { it.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull() }
        .filter { it != null }
        .flatMapLatest { it!!.currentLens }
        .flatMapLatest { it.flashLight }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val streams: SharedFlow<List<StreamParticipant>> =
        MutableSharedFlow<List<StreamParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val uiStreams = ConcurrentLinkedQueue<StreamParticipant>()
            participants.forEachParticipant(viewModelScope + CoroutineName("StreamParticipant")) { participant, itsMe, streams, state ->
                if (itsMe || (state == CallParticipant.State.InCall && streams.isNotEmpty())) {
                    val newStreams = streams.map {
                        StreamParticipant(
                            participant,
                            itsMe,
                            it,
                            usersDescription.name(listOf(participant.userId)),
                            usersDescription.image(listOf(participant.userId))
                        )
                    }
                    val currentStreams = uiStreams.filter { it.participant == participant }
                    val addedStreams = newStreams - currentStreams.toSet()
                    val removedStreams = currentStreams - newStreams.toSet()
                    uiStreams += addedStreams
                    uiStreams -= removedStreams.toSet()

                    removedStreams.map { it.stream.id }.forEach { _removedStreams.emit(it) }
                } else {
                    uiStreams
                        .filter { it.participant == participant }
                        .map { it.stream.id }
                        .forEach { _removedStreams.emit(it) }
                    uiStreams.removeAll { it.participant == participant }
                }
                emit(uiStreams.toList())
            }.launchIn(viewModelScope)
        }

    private val myStreams: Flow<List<Stream>> =
        participants.map { it.me }.flatMapLatest { it.streams }

    private val otherStreams: Flow<List<Stream>> =
        streams.transform { value -> emit(value.filter { !it.itsMe }.map { it.stream }) }

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

    private val cameraStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.video.firstOrNull { it is Input.Video.Camera } != null } }

    private val audioStream: Flow<Stream?> =
        myStreams.map { streams -> streams.firstOrNull { stream -> stream.audio.firstOrNull { it != null } != null } }

    val cameraEnabled: StateFlow<Boolean> =
        cameraStream
            .filter { it != null }
            .flatMapLatest { it!!.video }
            .filter { it != null }
            .flatMapLatest { it!!.enabled }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val micEnabled: StateFlow<Boolean> =
        audioStream
            .filter { it != null }
            .flatMapLatest { it!!.audio }
            .filter { it != null }
            .flatMapLatest { it!!.enabled }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val amIAlone: SharedFlow<Boolean> = combine(
        otherStreams,
        myLiveStreams
    ) { otherStreams, myLiveStreams -> otherStreams.isEmpty() || myLiveStreams.isEmpty() }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val inCallParticipants: SharedFlow<List<CallParticipant>> =
        MutableSharedFlow<List<CallParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val participants = ConcurrentHashMap<String, CallParticipant>()
            this@CallViewModel.participants.forEachParticipant(
                viewModelScope + CoroutineName("InCallParticipants")
            ) { participant, itsMe, streams, state ->
                if (itsMe || state == CallParticipant.State.InCall || streams.isNotEmpty()) participants[participant.userId] =
                    participant
                else participants.remove(participant.userId)
                emit(participants.values.toList())
            }.launchIn(viewModelScope)
        }

    val onParticipantJoin: SharedFlow<CallParticipant> =
        MutableSharedFlow<CallParticipant>().apply {
            var participants = listOf<CallParticipant>()
            inCallParticipants
                .onEach {
                    val diff = it.minus(participants.toSet())
                    diff.forEach { p -> emit(p) }
                    participants = it
                }.launchIn(viewModelScope)
        }

    val onParticipantLeave: SharedFlow<CallParticipant> =
        MutableSharedFlow<CallParticipant>().apply {
            var participants = listOf<CallParticipant>()
            inCallParticipants
                .onEach {
                    val left = participants.minus(it.toSet())
                    left.forEach { p -> emit(p) }
                    participants = it
                }.launchIn(viewModelScope)
        }

    private val _removedStreams: MutableSharedFlow<String> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val removedStreams: SharedFlow<String> = _removedStreams.asSharedFlow()

    val livePointerEvents: SharedFlow<Pair<String, Input.Video.Event.Pointer>> =
        MutableSharedFlow<Pair<String, Input.Video.Event.Pointer>>(
            replay = 1,
            extraBufferCapacity = 1
        ).apply {
            val jobs = mutableMapOf<String, List<Job>>()
            participants.forEachParticipant(viewModelScope) { participant, _, streams, _ ->
                jobs[participant.userId]?.forEach {
                    it.cancel()
                    it.join()
                }
                jobs.remove(participant.userId)
                streams.forEach { stream ->
                    val streamId = stream.id
                    val participantJobs = mutableListOf<Job>()
                    participantJobs += stream.video
                        .filter { it != null }
                        .flatMapLatest { it!!.events }
                        .filterIsInstance<Input.Video.Event.Pointer>()
                        .onEach { emit(Pair(streamId, it)) }
                        .launchIn(viewModelScope)
                    jobs[participant.userId] = participantJobs
                }
            }.launchIn(viewModelScope)
        }

    val requestMuteEvents: Flow<Input.Audio.Event.Request.Mute> = audioStream
        .filter { it != null }
        .flatMapLatest { it!!.audio }
        .filter { it != null }
        .flatMapLatest { it!!.events }
        .filterIsInstance()

    private val chat: StateFlow<ChatUI?> =
        participants
            .filter { it.others.isNotEmpty() }
            .map { CollaborationUI.chatBox.create(it.others.first()) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val areThereNewMessages = chat
        .filter { it != null }
        .flatMapLatest { it!!.messages }
        .map { it.other }
        .filter { it.isNotEmpty() }
        .flatMapLatest { it.first().state }
        .map { it is Message.State.Received }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun showChat(context: Context) = chat.value?.let { CollaborationUI.chatBox.show(context, it) }

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
                pJobs += participant.streams.combine(participant.state) { streams, state ->
                    action(participant, participant == participants.me, streams, state)
                }.launchIn(scope)
            }
        }
    }
}


