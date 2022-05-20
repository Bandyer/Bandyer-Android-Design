package com.kaleyra.collaboration_suite_core_ui.notification

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
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
    val usersList: List<String>
)

class ChatNotificationManager2(private val chatNotificationActivityClazz: Class<*>) :
    Application.ActivityLifecycleCallbacks {

    private val application = ContextRetainer.context.applicationContext as? Application
    private var isNotificationShown = false
    private var context: Activity? = null

    /**
     * Do Not Disturb flag. If set to true, the notifications are no longer shown.
     */
    var dnd: Boolean = false

    fun notify(notification: ChatNotification) {
        if (dnd || isNotificationShown) return
        application?.registerActivityLifecycleCallbacks(this)
        startNotificationActivity(notification)
    }

    fun dispose() {
        application?.unregisterActivityLifecycleCallbacks(this)
//        context = null
    }

    private fun startNotificationActivity(notification: ChatNotification) {
        val currentContext = context ?: ContextRetainer.context
        val intent = Intent(currentContext, chatNotificationActivityClazz).apply {
            context ?: addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra ("username", notification.name)
            putExtra("userId", notification.userId)
            putExtra("message", notification.message)
            putExtra("imageUri", notification.imageUri)
            putExtra("participants", notification.usersList.toTypedArray())
        }
        currentContext.startActivity(intent)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.javaClass != chatNotificationActivityClazz) return
        isNotificationShown = true
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) {
        context = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (context != activity) return
        context = null
    }

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.javaClass != chatNotificationActivityClazz) return
//        application?.unregisterActivityLifecycleCallbacks(this)
        isNotificationShown = false
    }

    companion object {
        const val AUTO_DISMISS_TIME = 3000L
    }
}