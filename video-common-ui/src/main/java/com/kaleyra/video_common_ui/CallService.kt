/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.call.CallNotificationDelegate
import com.kaleyra.video_common_ui.call.CallNotificationDelegate.Companion.CALL_NOTIFICATION_ID
import com.kaleyra.video_common_ui.call.CameraStreamInputsDelegate
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import com.kaleyra.video_common_ui.call.ScreenShareOverlayDelegate
import com.kaleyra.video_common_ui.call.StreamsOpeningDelegate
import com.kaleyra.video_common_ui.call.StreamsVideoViewDelegate
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate
import com.kaleyra.video_common_ui.proximity.CallProximityDelegate
import com.kaleyra.video_common_ui.proximity.ProximityCallActivity
import com.kaleyra.video_common_ui.texttospeech.AwaitingParticipantsTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.CallParticipantMutedTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.CallRecordingTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.TextToSpeechNotifier
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.plus

/**
 * The CallService
 */
internal class CallService : LifecycleService(), CameraStreamPublisher, CameraStreamInputsDelegate,
    StreamsOpeningDelegate, StreamsVideoViewDelegate, CallNotificationDelegate,
    FileShareNotificationDelegate, ScreenShareOverlayDelegate, ActivityLifecycleCallbacks {

    companion object {
        fun start() = with(ContextRetainer.context) {
            com.kaleyra.video_common_ui.CallService.Companion.stop()
            val intent = Intent(this, com.kaleyra.video_common_ui.CallService::class.java)
            startService(intent)
        }

        fun stop() = with(ContextRetainer.context) {
            stopService(Intent(this, com.kaleyra.video_common_ui.CallService::class.java))
        }
    }

    private var notification: Notification? = null
        @Synchronized get
        @Synchronized set

    private var foregroundJob: Job? = null

    private var proximityDelegate: CallProximityDelegate<LifecycleService>? = null

    private var proximityCallActivity: ProximityCallActivity? = null

    private var call: com.kaleyra.video_common_ui.CallUI? = null

    private var onCallNewActivity: ((Context) -> Unit)? = null

    private var recordingTextToSpeechNotifier: TextToSpeechNotifier? = null

    private var awaitingParticipantsTextToSpeechNotifier: TextToSpeechNotifier? = null

    private var mutedTextToSpeechNotifier: TextToSpeechNotifier? = null

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Thread.setDefaultUncaughtExceptionHandler(com.kaleyra.video_common_ui.CallUncaughtExceptionHandler)
        setUpCall()
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        clearNotification()
        application.unregisterActivityLifecycleCallbacks(this)
        recordingTextToSpeechNotifier?.dispose()
        mutedTextToSpeechNotifier?.dispose()
        awaitingParticipantsTextToSpeechNotifier?.dispose()
        proximityDelegate?.destroy()
        foregroundJob?.cancel()
        call?.end()
        awaitingParticipantsTextToSpeechNotifier = null
        recordingTextToSpeechNotifier = null
        mutedTextToSpeechNotifier = null
        proximityCallActivity = null
        proximityDelegate = null
        onCallNewActivity = null
        foregroundJob = null
        notification = null
        call = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity::class.java != call?.activityClazz) return
        onCallNewActivity?.invoke(activity)
        if (activity !is ProximityCallActivity) return
        proximityCallActivity = activity
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (activity::class.java != call?.activityClazz || activity !is ProximityCallActivity) return
        proximityCallActivity = null
    }

    /**
     * Set up the call streams and notifications
     *
     */
    private fun setUpCall() {
        com.kaleyra.video_common_ui.KaleyraVideo.onCallReady(lifecycleScope) { call ->
            application.registerActivityLifecycleCallbacks(this)
            this@CallService.call = call

            addCameraStream(call)
            handleCameraStreamAudio(call, lifecycleScope)
            handleCameraStreamVideo(call, lifecycleScope)
            openParticipantsStreams(call.participants, lifecycleScope)
            setStreamsVideoView(this@CallService, call.participants, lifecycleScope)
            syncCallNotification(call, call.activityClazz, lifecycleScope)

            call.participants
                .onEach { participants ->
                    val userIds = participants.list.map { it.userId }.toTypedArray()
                    ContactDetailsManager.refreshContactDetails(*userIds)
                }
                .launchIn(lifecycleScope)

            var screenShareScope: CoroutineScope? = null
            if (!DeviceUtils.isSmartGlass) {
                handleProximity(call)
                syncFileShareNotification(this, call, call.activityClazz, lifecycleScope)
                onCallNewActivity = { activityContext ->
                    screenShareScope?.cancel()
                    screenShareScope = newChildScope(coroutineScope = lifecycleScope, dispatcher = Dispatchers.Main)
                    syncScreenShareOverlay(activityContext, call, screenShareScope!!)
                }
            }

            call.state
                .takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion {
                    stopSelf()
                    screenShareScope = null
                }
                .launchIn(lifecycleScope)
        }
    }

    //////////////////////////////
    // CallNotificationDelegate //
    //////////////////////////////
    /**
     * @suppress
     */
    override fun showNotification(notification: Notification) {
        this.notification = notification
        super.showNotification(notification)
        moveToForegroundWhenPossible()
    }

    /**
     * @suppress
     */
    @Suppress("DEPRECATION")
    override fun clearNotification() {
        super.clearNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else stopForeground(true)
    }

    private fun moveToForegroundWhenPossible() {
        if (foregroundJob != null) return
        // Every time the app goes in foreground, try to promote the service in foreground.
        // The runCatching is needed because the startForeground may fails when the app is in background but
        // the isInForeground flag is still true. This happens because the onStop of the application lifecycle is
        // dispatched 700ms after the last activity's onStop
        foregroundJob = combine(AppLifecycle.isInForeground, flowOf(call!!).hasScreenSharingInput()) { isInForeground, hasScreenSharingPermission ->
            if (!isInForeground) return@combine
            kotlin.runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(CALL_NOTIFICATION_ID, notification!!, getForegroundServiceType(hasScreenSharingPermission))
                else startForeground(CALL_NOTIFICATION_ID, notification!!)
            }
        }.launchIn(lifecycleScope)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getForegroundServiceType(hasScreenSharingPermission: Boolean): Int {
        val inputsFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        val screenSharingFlag = if (hasScreenSharingPermission) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
        return ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or inputsFlag or screenSharingFlag
    }

    private fun handleProximity(call: com.kaleyra.video_common_ui.CallUI) {
        combine(
            call.state,
            call.participants
        ) { state, participants -> state is Call.State.Disconnected && participants.let { it.creator() != it.me && it.creator() != null } }
            .onEach {
                // if the call is incoming, don't immediately bind the proximity
                if (it) return@onEach
                proximityDelegate = CallProximityDelegate<LifecycleService>(
                    lifecycleContext = this,
                    call = call,
                    disableProximity = { proximityCallActivity?.disableProximity ?: false },
                    disableWindowTouch = { disableWindowTouch ->
                        if (disableWindowTouch) proximityCallActivity?.disableWindowTouch()
                        else proximityCallActivity?.enableWindowTouch()
                    }
                ).apply { bind() }
                recordingTextToSpeechNotifier = CallRecordingTextToSpeechNotifier(
                    call,
                    proximityDelegate!!.sensor!!
                ).apply { start(lifecycleScope) }
                mutedTextToSpeechNotifier = CallParticipantMutedTextToSpeechNotifier(
                    call,
                    proximityDelegate!!.sensor!!
                ).apply { start(lifecycleScope) }
                awaitingParticipantsTextToSpeechNotifier = AwaitingParticipantsTextToSpeechNotifier(
                    call,
                    proximityDelegate!!.sensor!!
                ).apply { start(lifecycleScope) }
            }
            .takeWhile { it }
            .launchIn(lifecycleScope)
    }

    private fun newChildScope(coroutineScope: CoroutineScope, dispatcher: CoroutineDispatcher) =
        CoroutineScope(SupervisorJob(coroutineScope.coroutineContext[Job])) + dispatcher
}