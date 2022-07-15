package com.kaleyra.collaboration_suite_core_ui

import android.app.Notification
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite_core_ui.call.CallNotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallNotificationDelegate.Companion.CALL_NOTIFICATION_ID
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

/**
 * The CallService
 */
class CallService : LifecycleService(), CallStreamDelegate, CallNotificationDelegate {

    internal companion object {
        const val CALL_ACTIVITY_CLASS = "call_activity_class"
        private val TAG = this::class.java.name
    }

    private var notification: Notification? = null
        @Synchronized get
        @Synchronized set

    private var foregroundJob: Job? = null

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val callActivityClazz = intent?.extras?.getSerializable(CALL_ACTIVITY_CLASS) as? Class<*>
        callActivityClazz ?: kotlin.run {
            stopSelf()
            Log.e(TAG, "Call Activity Class not provided!")
            return START_NOT_STICKY
        }
        setUpCall(callActivityClazz)
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        clearNotification()
    }

    /**
     * Set up the call streams and notifications
     *
     * @param callActivityClazz The call activity class
     */
    private fun setUpCall(callActivityClazz: Class<*>) {
        CollaborationUI.onCallReady(lifecycleScope) {
            setUpCallStreams(this@CallService, it)
            syncCallNotification(
                it,
                CollaborationUI.usersDescription,
                callActivityClazz,
                lifecycleScope
            )
        }
    }

    //////////////////////////////
    // CallNotificationDelegate //
    //////////////////////////////
    /**
     * @suppress
     */
    override fun showNotification(notification: Notification) {
        super.showNotification(notification)
        this.notification = notification
        moveToForegroundWhenPossible()
    }

    /**
     * @suppress
     */
    override fun clearNotification() {
        super.clearNotification()
        stopForeground(true)
    }

    private fun moveToForegroundWhenPossible() {
        if (foregroundJob != null) return
        foregroundJob = AppLifecycle.isInForeground
            .dropWhile { !it }
            .take(1)
            .onEach { startForeground(CALL_NOTIFICATION_ID, notification!!) }
            .launchIn(lifecycleScope)
    }
}