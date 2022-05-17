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

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.Participant
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.call.CallUIController
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.model.Volume
import com.kaleyra.collaboration_suite_glass_ui.model.internal.StreamParticipant
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("UNCHECKED_CAST")
internal class CallViewModelFactory(
    private val callDelegate: CallUIDelegate,
    private val deviceStatusDelegate: DeviceStatusDelegate,
    private val callController: CallUIController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CallViewModel(callDelegate, deviceStatusDelegate, callController) as T
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class CallViewModel(
    callDelegate: CallUIDelegate,
    deviceStatusDelegate: DeviceStatusDelegate,
    private val callController: CallUIController
) : ViewModel() {

    val call: SharedFlow<Call> = callDelegate.call

    val hasSwitchCamera: StateFlow<Boolean> = MutableStateFlow(false).apply {
        call
            .flatMapLatest { it.inputs.allowList }
            .map { it.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull() }
            .map { c -> c?.lenses?.firstOrNull { it.isRear } != null && c.lenses.firstOrNull { !it.isRear } != null }
            .onEach { value = it }
            .launchIn(viewModelScope)
    }

    val zoom: StateFlow<Input.Video.Camera.Internal.Zoom?> = call
        .flatMapLatest { it.inputs.allowList }
        .map { it.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull() }
        .filter { it != null }
        .flatMapLatest { it!!.currentLens }
        .flatMapLatest { it.zoom }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val flashLight: StateFlow<Input.Video.Camera.Internal.FlashLight?> = call
        .flatMapLatest { it.inputs.allowList }
        .map { it.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull() }
        .filter { it != null }
        .flatMapLatest { it!!.currentLens }
        .flatMapLatest { it.flashLight }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val preferredCallType get() = call.replayCache.first().extras.preferredType

    val whiteboard = call.mapLatest { it.whiteboard }

    val callState = call.flatMapLatest { it.state }

    val participants = call.flatMapLatest { it.participants }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val battery: SharedFlow<BatteryInfo> = deviceStatusDelegate.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusDelegate.wifi

    val usersDescription: UsersDescription = callDelegate.callUsersDescription

    val volume: Volume get() = callController.onGetVolume()

    val callDuration = call.flatMapLatest { it.extras.duration }

    val callTimeToLive = call.flatMapLatest { it.constraints.timeToLive }

    val inCallParticipants: SharedFlow<List<CallParticipant>> =
        MutableSharedFlow<List<CallParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val participants = ConcurrentHashMap<String, CallParticipant>()
            this@CallViewModel.participants.forEachParticipant(
                viewModelScope + CoroutineName("InCallParticipants")
            ) { participant, itsMe, streams, state ->
                if (itsMe || state == CallParticipant.State.IN_CALL || streams.isNotEmpty()) participants[participant.userId] =
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

    val streams: SharedFlow<List<StreamParticipant>> =
        MutableSharedFlow<List<StreamParticipant>>(replay = 1, extraBufferCapacity = 1).apply {
            val uiStreams = ConcurrentLinkedQueue<StreamParticipant>()
            participants.forEachParticipant(viewModelScope + CoroutineName("StreamParticipant")) { participant, itsMe, streams, state ->
                if (itsMe || (state == CallParticipant.State.IN_CALL && streams.isNotEmpty())) {
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
        MutableStateFlow(
            Permission(
                isAllowed = call.replayCache.first().inputs.allowList.value.filterIsInstance<Input.Audio>()
                    .firstOrNull() != null,
                neverAskAgain = false
            )
        )
    val micPermission: StateFlow<Permission> = _micPermission.asStateFlow()

    private val _camPermission: MutableStateFlow<Permission> =
        MutableStateFlow(
            Permission(
                isAllowed = call.replayCache.first().inputs.allowList.value.filterIsInstance<Input.Video.Camera.Internal>()
                    .firstOrNull() != null,
                neverAskAgain = false
            )
        )
    val camPermission: StateFlow<Permission> = _camPermission.asStateFlow()

    val amIAlone: Flow<Boolean> = combine(
        otherStreams,
        myLiveStreams,
        camPermission
    ) { otherStreams, myLiveStreams, camPermission -> !(otherStreams.isNotEmpty() && (myLiveStreams.isNotEmpty() || !camPermission.isAllowed)) }

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
                        .filter { it is Input.Video.Event.Pointer }
                        .onEach { emit(Pair(streamId, it as Input.Video.Event.Pointer)) }
                        .launchIn(viewModelScope)
                    jobs[participant.userId] = participantJobs
                }
            }.launchIn(viewModelScope)
        }

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

    fun onSwitchCamera() = callController.onSwitchCamera()

    fun onAnswer() = callController.onAnswer()

    fun onHangup() = callController.onHangup()

    fun onSetVolume(value: Int) = callController.onSetVolume(value)

    fun onSetZoom(value: Float) = callController.onSetZoom(value)
}


