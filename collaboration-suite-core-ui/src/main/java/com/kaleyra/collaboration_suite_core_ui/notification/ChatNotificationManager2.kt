package com.kaleyra.collaboration_suite_core_ui.notification

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * NotificationData
 *
 * @property name The user name
 * @property userId The user identifier
 * @property message The message sent by the user
 * @property imageUri The avatar image uri
 * @constructor
 */
data class ChatNotification(
    val name: String,
    val userId: String,
    val message: String,
    val imageUri: Uri = Uri.EMPTY,
)

class ChatNotificationManager2(private val chatNotificationActivityClazz: Class<*>): Application.ActivityLifecycleCallbacks {

    private val application = ContextRetainer.context.applicationContext as Application
    private var isNotificationShown = false

    /**
     * Do Not Disturb flag. If set to true, the notifications are no longer shown.
     */
    var dnd: Boolean = false

    fun notify(notification: ChatNotification) {
        if (dnd || isNotificationShown) return
        application.registerActivityLifecycleCallbacks(this)
        startNotificationActivity(notification)
    }

    private fun startNotificationActivity(notification: ChatNotification) =
        with(ContextRetainer.context) {
            val intent = Intent(this, chatNotificationActivityClazz).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("username", notification.name)
                putExtra("userId", notification.userId)
                putExtra("message", notification.message)
                putExtra("imageUri", notification.imageUri)
            }
            startActivity(intent)
        }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.javaClass != chatNotificationActivityClazz) return
        isNotificationShown = true
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.javaClass != chatNotificationActivityClazz) return
        application.unregisterActivityLifecycleCallbacks(this)
        isNotificationShown = false
    }
}