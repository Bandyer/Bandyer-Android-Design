package com.kaleyra.collaboration_suite_core_ui

import android.app.Notification
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite_core_ui.call.CallNotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

/**
 * The CallService
 */
class CallService : LifecycleService(),
    CallStreamDelegate,
    CallNotificationDelegate,
    CallNotificationActionReceiver.ActionDelegate {

    internal companion object {
        const val CALL_NOTIFICATION_ID = 22
        const val CALL_ACTIVITY_CLASS = "call_activity_class"
    }

    private var call: CallUI? = null

    private var isServiceInForeground: Boolean = false

    /**
     * @suppress
     */
    override fun onCreate() {
        super.onCreate()
        CallNotificationActionReceiver.actionDelegate = this
    }

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val callActivityClazz = intent?.extras?.getSerializable(CALL_ACTIVITY_CLASS) as? Class<*>
        callActivityClazz ?: kotlin.run {
            stopSelf()
            Log.e("CallService", "Unable to get the call activity class")
            return START_NOT_STICKY
        }
        bindCall(callActivityClazz)
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        clearNotification()
        CallNotificationActionReceiver.actionDelegate = null
    }

    /**
     * Bind the service to a phone box
     *
     * @param callActivityClazz The call activity class
     */
    private fun bindCall(callActivityClazz: Class<*>) {
        CollaborationUI.phoneBox.call
            .take(1)
            .onEach {
                call = it
                setUpCallStreams(this@CallService, it)
                syncCallNotification(it, CollaborationUI.usersDescription, callActivityClazz)
            }.launchIn(lifecycleScope)
    }

    ///////////////////////////////////////////////////
    // CallNotificationActionReceiver.ActionDelegate //
    ///////////////////////////////////////////////////
    /**
     * @suppress
     */
    override fun onAnswerAction(): Unit = let { call?.connect() }

    /**
     * @suppress
     */
    override fun onHangUpAction(): Unit = let { call?.end() }

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