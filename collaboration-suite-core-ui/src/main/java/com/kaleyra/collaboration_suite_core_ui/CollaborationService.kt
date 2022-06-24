package com.kaleyra.collaboration_suite_core_ui

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite_core_ui.call.CallNotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import kotlinx.coroutines.flow.launchIn
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

    companion object {
        private const val CALL_NOTIFICATION_ID = 22
        const val CALL_ACTIVITY_CLASS = "call_activity_class"
    }

    private var currentCall: CallUI? = null

    private var callActivityClazz: Class<*>? = null

    private var isServiceInForeground: Boolean = false

    override val isAppInForeground: Boolean get() = AppLifecycle.isInForeground

    /**
     * @suppress
     */
    override fun onCreate() {
        super.onCreate()
        application.registerActivityLifecycleCallbacks(this)
        CallNotificationActionReceiver.actionDelegate = this
    }

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val call = CollaborationUI.phoneBox.call.replayCache.firstOrNull()
        val callActivityClass = intent?.extras?.getSerializable(CALL_ACTIVITY_CLASS) as? Class<*>
        call ?: callActivityClass ?: kotlin.run {
            stopSelf()
            return START_NOT_STICKY
        }
        currentCall = call
        callActivityClazz = callActivityClass
        bindCall(currentCall!!, callActivityClazz!!)
        observeAppLifecycle()
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        clearNotification()
        application.unregisterActivityLifecycleCallbacks(this)
        CallNotificationActionReceiver.actionDelegate = null
        currentCall = null
        callActivityClazz = null
    }

    /**
     * Bind the service to a phone box
     *
     * @param callActivityClazz The call activity class
     */
    private fun bindCall(
        call: CallUI,
        callActivityClazz: Class<*>
    ) = lifecycleScope.launch {
        setUpCallStreams(this@CollaborationService, call)
        syncNotificationWithCallState(
            this@CollaborationService,
            call,
            CollaborationUI.usersDescription,
            callActivityClazz
        )
    }

    private fun observeAppLifecycle() {
        AppLifecycle.isInForegroundFlow
            .onEach {
                if (!it) return@onEach
                moveNotificationToForeground(
                    currentCall!!,
                    CollaborationUI.usersDescription,
                    callActivityClazz!!
                )
            }
            .takeWhile { !it }
            .launchIn(lifecycleScope)
    }

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
    override fun onActivityStarted(activity: Activity) = Unit

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