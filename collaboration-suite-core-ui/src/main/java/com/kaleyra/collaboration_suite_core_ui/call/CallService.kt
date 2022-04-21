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

package com.kaleyra.collaboration_suite_core_ui.call

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import com.kaleyra.collaboration_suite_core_ui.UIProvider
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.model.Volume
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.collaboration_suite_utils.audio.CallAudioManager
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryObserver
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiObserver
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/**
 * @suppress
 */
class CallService : BoundService(), CallUIDelegate, CallUIController, DeviceStatusDelegate,
    CallNotificationActionReceiver.ActionDelegate, DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private companion object {
        const val CALL_NOTIFICATION_ID = 22
        const val MY_STREAM_ID = "main"
    }

    private var activityClazz: Class<*>? = null

    private var isAppInForeground = false
    private var isServiceInForeground = false

    private var phoneBox: PhoneBox? = null
    private var phoneBoxJob: Job? = null

    private var currentCall: Call? = null

    private var batteryObserver: BatteryObserver? = null
    private var wifiObserver: WiFiObserver? = null

    private var callAudioManager: CallAudioManager? = null

    private val ongoingCall: MutableSharedFlow<Call> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    override val call: SharedFlow<Call>
        get() = ongoingCall.asSharedFlow()

    override var usersDescription: UsersDescription = UsersDescription()

    override val battery: SharedFlow<BatteryInfo>
        get() = batteryObserver!!.observe()

    override val wifi: SharedFlow<WiFiInfo>
        get() = wifiObserver!!.observe()

    // Service
    override fun onCreate() {
        super<BoundService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)

        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)
        callAudioManager = CallAudioManager(this)

        CallNotificationActionReceiver.actionDelegate = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super<BoundService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)

        clearNotification()

        phoneBoxJob?.cancel()
        currentCall?.end()
        phoneBox?.disconnect()
        batteryObserver?.stop()
        wifiObserver?.stop()

        activityClazz = null
        currentCall = null
        phoneBox = null
        phoneBoxJob = null
        batteryObserver = null
        wifiObserver = null
        callAudioManager = null
        CallNotificationActionReceiver.actionDelegate = null
    }

    // DefaultLifecycleObserver
    override fun onStart(owner: LifecycleOwner) {
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
    }

    // ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity.javaClass != activityClazz) return
        publishMyStream(currentCall!!, activity as FragmentActivity)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity.javaClass != activityClazz || isServiceInForeground) return
        startForegroundIfIncomingCall()
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    override suspend fun onRequestMicPermission(context: FragmentActivity): Permission =
        if (currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Audio } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall?.inputs?.request(context, Inputs.Type.Microphone)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }

    override suspend fun onRequestCameraPermission(context: FragmentActivity): Permission =
        if (currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Video.Camera.Internal } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall?.inputs?.request(context, Inputs.Type.Camera.Internal)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }

    override fun onAnswer() = currentCall?.connect() ?: Unit

    override fun onHangup() {
        currentCall?.end() ?: Unit
        clearNotification()
    }

    override fun onEnableCamera(enable: Boolean) {
        val video =
            currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Video.Camera.Internal }
                ?: return
        if (enable) video.tryEnable() else video.tryDisable()
    }

    override fun onEnableMic(enable: Boolean) {
        val audio =
            currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Audio }
                ?: return
        if (enable) audio.tryEnable() else audio.tryDisable()
    }

    override fun onSwitchCamera() {
        val camera = currentCall?.inputs?.allowList?.value?.filterIsInstance<Input.Video.Camera.Internal>()?.firstOrNull()
        val currentLens = camera?.currentLens?.value
        val newLens = camera?.lenses?.firstOrNull { it.isRear != currentLens?.isRear } ?: return
        camera.setLens(newLens)
    }

    override fun onGetVolume(): Volume = Volume(
        callAudioManager!!.currentVolume,
        callAudioManager!!.minVolume,
        callAudioManager!!.maxVolume
    )

    override fun onSetVolume(value: Int) = callAudioManager!!.setVolume(value)

    override fun onSetZoom(value: Float) {
        val camera = currentCall?.inputs?.allowList?.value?.filterIsInstance<Input.Video.Camera.Internal>()?.firstOrNull()
        val currentLens = camera?.currentLens?.value ?: return
        currentLens.zoom.tryZoom(value)
    }

    // CallService
    fun bind(
        phoneBox: PhoneBox,
        usersDescription: UsersDescription? = null,
        activityClazz: Class<*>
    ) {
        this.phoneBox = phoneBox
        this.usersDescription = usersDescription ?: UsersDescription()
        this.activityClazz = activityClazz
        phoneBoxJob?.cancel()
        phoneBoxJob = observeCall(phoneBox)
    }

    private fun observeCall(phoneBox: PhoneBox): Job =
        phoneBox.call.onEach onEachCall@{ call ->
            if (currentCall != null || call.state.value is Call.State.Disconnected.Ended) return@onEachCall

            currentCall = call
            call.state
                .takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion { currentCall = null }
                .launchIn(lifecycleScope)

            setupCall(call)

            ongoingCall.emit(call)
            val participants = call.participants.value
            if (isAppInForeground && (!this@CallService.isSilent() || participants.me == participants.creator()))
                UIProvider.showCall(activityClazz!!)
        }.launchIn(lifecycleScope)

    private suspend fun setupCall(call: Call) {
        val coroutineScope = MainScope() + CoroutineName("call scope: ${call.id}")
        setUpStreamsAndVideos(call, coroutineScope = coroutineScope)
        updateStreamInputsOnPermissions(call, coroutineScope = coroutineScope)
        syncNotificationWithCallState(call)
        clearScopeAndNotificationOnCallEnd(call, scopeToCancel = coroutineScope)
    }

    private fun publishMyStream(call: Call, fragmentActivity: FragmentActivity) {
        val me = call.participants.value.me
        if (me.streams.value.firstOrNull { it.id == MY_STREAM_ID } != null) return
        me.addStream(fragmentActivity, MY_STREAM_ID).let {
            it.audio.value = null
            it.video.value = null
        }
    }

    private fun updateStreamInputsOnPermissions(call: Call, coroutineScope: CoroutineScope) {
        val hasVideo = call.extras.preferredType.hasVideo()

        call.inputs.allowList.onEach { inputs ->
            if (inputs.isEmpty()) return@onEach

            val videoInput = inputs.lastOrNull { it is Input.Video.My } as? Input.Video.My
            val audioInput = inputs.firstOrNull { it is Input.Audio } as? Input.Audio

            videoInput?.setQuality(Input.Video.Quality.Definition.HD)

            val me = call.participants.value.me
            me.streams.value.firstOrNull { it.id == MY_STREAM_ID }?.let {
                it.audio.value = audioInput
                if (hasVideo) it.video.value = videoInput
            }

        }.launchIn(coroutineScope)
    }

    private fun setUpStreamsAndVideos(call: Call, coroutineScope: CoroutineScope) {
        val pJobs = mutableListOf<Job>()
        val sJobs = hashMapOf<String, List<Job>>()
        call.participants
            .map { it.others + it.me }
            .onEach onEachParticipants@{ participants ->
                pJobs.forEach {
                    it.cancel()
                    it.join()
                }
                pJobs.clear()

                sJobs.values.forEach { jobs ->
                    jobs.forEach {
                        it.cancel()
                        it.join()
                    }
                }
                sJobs.clear()

                participants.forEach { participant ->
                    pJobs += participant.streams
                        .onEach onEachStreams@{ streams ->
                            sJobs[participant.userId]?.forEach {
                                it.cancel()
                                it.join()
                            }
                            val streamsJobs = mutableListOf<Job>()
                            streams.forEach { stream ->
                                stream.open()
                                streamsJobs += stream.video.onEach { video ->
                                    if (video?.view?.value != null) return@onEach
                                    video?.view?.value = VideoStreamView(applicationContext)
                                }.launchIn(coroutineScope)
                            }
                            sJobs[participant.userId] = streamsJobs
                        }.launchIn(coroutineScope)
                }
            }.launchIn(coroutineScope)
    }

    private suspend fun syncNotificationWithCallState(call: Call) {
        val participants = call.participants.value
        val callerDescription = usersDescription.name(listOf(participants.creator()?.userId ?: ""))
        val calleeDescription = usersDescription.name(participants.others.map { it.userId })
        val isGroupCall = participants.others.count() > 1

        if (participants.me != participants.creator())
            showIncomingCallNotification(
                usersDescription = callerDescription,
                isGroupCall = isGroupCall,
                isHighPriority = !isAppInForeground || this@CallService.isSilent(),
                moveToForeground = isAppInForeground
            )

        call.state
            .onEach {
                when {
                    it is Call.State.Connecting && participants.me == participants.creator() ->
                        showOutgoingCallNotification(usersDescription = calleeDescription, isGroupCall = isGroupCall)
                    it is Call.State.Connecting || it is Call.State.Connected -> showOnGoingCallNotification(
                        usersDescription = calleeDescription,
                        isGroupCall = isGroupCall,
                        isCallRecorded = call.extras.recording is Call.Recording.OnConnect,
                        isConnecting = it is Call.State.Connecting
                    )
                }
            }
            .takeWhile { it !is Call.State.Connected }
            .launchIn(lifecycleScope)
    }

    private fun clearScopeAndNotificationOnCallEnd(call: Call, scopeToCancel: CoroutineScope) {
        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion {
                scopeToCancel.cancel()
                clearNotification()
            }.launchIn(lifecycleScope)
    }

    private fun clearNotification() {
        stopForegroundLocal()
        NotificationManager.cancelNotification(CALL_NOTIFICATION_ID)
    }

    private fun startForegroundIfIncomingCall() =
        lifecycleScope.launch {
            val participants = currentCall!!.participants.value
            if (currentCall!!.state.value !is Call.State.Disconnected || participants.me == participants.creator()) return@launch
            showIncomingCallNotification(
                usersDescription.name(listOf(participants.creator()?.userId ?: "")),
                participants.others.count() > 1,
                isHighPriority = false,
                moveToForeground = true
            )
        }

    private fun showIncomingCallNotification(
        usersDescription: String,
        isGroupCall: Boolean,
        isHighPriority: Boolean,
        moveToForeground: Boolean
    ) {
        val notification = NotificationManager.buildIncomingCallNotification(
            user = usersDescription,
            isGroupCall = isGroupCall,
            activityClazz = activityClazz!!,
            isHighPriority = isHighPriority
        )
        showNotification(notification, moveToForeground)
    }

    private fun showOutgoingCallNotification(usersDescription: String, isGroupCall: Boolean) {
        val notification = NotificationManager.buildOutgoingCallNotification(
            user = usersDescription,
            isGroupCall = isGroupCall,
            activityClazz = activityClazz!!
        )
        showNotification(notification, true)
    }

    private fun showOnGoingCallNotification(
        usersDescription: String,
        isGroupCall: Boolean,
        isCallRecorded: Boolean,
        isConnecting: Boolean
    ) {
        val notification = NotificationManager.buildOngoingCallNotification(
            user = usersDescription,
            isGroupCall = isGroupCall,
            isCallRecorded = isCallRecorded,
            isSharingScreen = false,
            isConnecting = isConnecting,
            activityClazz = activityClazz!!
        )
        showNotification(notification, true)
    }

    private fun showNotification(notification: Notification, moveToForeground: Boolean) {
        if (moveToForeground) startForegroundLocal(notification)
        else NotificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    private fun startForegroundLocal(notification: Notification) =
        startForeground(CALL_NOTIFICATION_ID, notification).also { isServiceInForeground = true }

    private fun stopForegroundLocal() =
        stopForeground(true).also { isServiceInForeground = false }

    override fun onAnswerAction() { currentCall?.connect() }

    override fun onHangUpAction() { currentCall?.end() }

    override fun onScreenShareAction() = Unit
}