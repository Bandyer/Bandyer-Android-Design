package com.kaleyra.collaboration_suite_core_ui

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.call.CallNotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

/**
 * The CollaborationService
 */
class CollaborationService : BoundService(),
                             CallStreamDelegate,
                             CallNotificationDelegate,
                             Application.ActivityLifecycleCallbacks,
                             CallNotificationActionReceiver.ActionDelegate {

    private companion object {
        const val CALL_NOTIFICATION_ID = 22
    }

    private var usersDescription: UsersDescription = UsersDescription()

    private var callActivityClazz: Class<*>? = null

    private var isServiceInForeground: Boolean = false

    var currentCall: CallUI? = null

    override val isAppInForeground: Boolean get() = AppLifecycle.isInForeground.value

    /**
     * @suppress
     */
    override fun onCreate() {
        super.onCreate()
        application.registerActivityLifecycleCallbacks(this)
        CallNotificationActionReceiver.actionDelegate = this
        AppLifecycle.isInForeground.dropWhile { !it }.filterNot { it }.onEach {
            if (currentCall == null || currentCall!!.state.value is Call.State.Disconnected.Ended) stopSelf()
        }.launchIn(lifecycleScope)
    }

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        application.unregisterActivityLifecycleCallbacks(this)
        clearNotification()
        currentCall?.end()
        CollaborationUI.disconnect()
        CallNotificationActionReceiver.actionDelegate = null
        currentCall = null
        callActivityClazz = null
    }

    /**
     * Bind the service to a phone box
     *
     * @param usersDescription The user description. Optional.
     * @param callActivityClazz The call activity class
     */
    fun bindCall(
        call: CallUI,
        usersDescription: UsersDescription,
        callActivityClazz: Class<*>
    ) {
        if (currentCall != null || call.state.value is Call.State.Disconnected.Ended) return
        this.usersDescription = usersDescription
        this.callActivityClazz = callActivityClazz
        lifecycleScope.launch {
            currentCall = call
            call.state.takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion {
                    currentCall = null
                    if (isAppInForeground) return@onCompletion
                    stopSelf()
                }.launchIn(lifecycleScope)

            setUpCallStreams(this@CollaborationService, call)

            syncNotificationWithCallState(
                this@CollaborationService,
                call,
                this@CollaborationService.usersDescription,
                callActivityClazz
            )
        }
    }

    fun canShowCallActivity(call: Call): Boolean =
        isAppInForeground && (!this@CollaborationService.isSilent() || call.participants.value.let { it.me == it.creator() })

    ////////////////////////////////////////////
    // Application.ActivityLifecycleCallbacks //
    ////////////////////////////////////////////
    /**
     * @suppress
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.javaClass != callActivityClazz) return
        currentCall?.also { publishMyStream(activity as FragmentActivity, it) }
    }

    /**
     * @suppress
     */
    override fun onActivityStarted(activity: Activity) {
        if (activity.javaClass != callActivityClazz || isServiceInForeground) return
        lifecycleScope.launch {
            currentCall ?: return@launch
            moveNotificationToForeground(
                currentCall!!,
                usersDescription,
                callActivityClazz!!
            )
        }
    }

    /**
     * @suppress
     */
    override fun onActivityResumed(activity: Activity) = Unit

    /**
     * @suppress
     */
    override fun onActivityPaused(activity: Activity) = Unit

    /**
     * @suppress
     */
    override fun onActivityStopped(activity: Activity) = Unit

    /**
     * @suppress
     */
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    /**
     * @suppress
     */
    override fun onActivityDestroyed(activity: Activity) = Unit

    ///////////////////////////////////////////////////
    // CallNotificationActionReceiver.ActionDelegate //
    ///////////////////////////////////////////////////
    /**
     * @suppress
     */
    override fun onAnswerAction() {
        currentCall?.connect()
    }

    /**
     * @suppress
     */
    override fun onHangUpAction() {
        currentCall?.end()
    }

    /**
     * @suppress
     */
    override fun onScreenShareAction() = Unit

    /////////////////////
    // CallController //
    ////////////////////
//    /**
//     * @suppress
//     */
//    override fun onHangup() {
//        super.onHangup()
//        clearNotification()
//    }

    //////////////////////////////
    // CallNotificationDelegate //
    //////////////////////////////
    /**
     * @suppress
     */
    override fun showNotification(notification: Notification, showInForeground: Boolean) {
        if (showInForeground) {
            startForeground(CALL_NOTIFICATION_ID, notification).also {
                isServiceInForeground = true
            }
        } else NotificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    /**
     * @suppress
     */
    override fun clearNotification() {
        stopForeground(true).also { isServiceInForeground = false }
        NotificationManager.cancel(CALL_NOTIFICATION_ID)
    }
}