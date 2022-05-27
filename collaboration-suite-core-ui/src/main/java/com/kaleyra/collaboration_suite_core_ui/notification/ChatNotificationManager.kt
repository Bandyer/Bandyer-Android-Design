package com.kaleyra.collaboration_suite_core_ui.notification

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.kaleyra.collaboration_suite_utils.ContextRetainer

class ChatNotificationManager(private val chatNotificationActivityClazz: Class<*>) :
    Application.ActivityLifecycleCallbacks {

    private val application = ContextRetainer.context.applicationContext as? Application
    private var context: Activity? = null

    init {
        application?.registerActivityLifecycleCallbacks(this)
    }

    /**
     * Do Not Disturb flag. If set to true, the notifications are no longer shown.
     */
    var dnd: Boolean = false

    fun notify(notification: ChatNotification) {
        if (dnd) return
        startNotificationActivity(notification)
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) {
        if (context == chatNotificationActivityClazz) return
        context = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (context != activity) return
        context = null
    }

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    companion object {
        const val AUTO_DISMISS_TIME = 3000L
    }
}