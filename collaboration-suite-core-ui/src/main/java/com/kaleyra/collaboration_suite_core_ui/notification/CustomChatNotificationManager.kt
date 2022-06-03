package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.Intent
import com.kaleyra.collaboration_suite_utils.ContextRetainer


class CustomChatNotificationManager(private val chatNotificationActivityClazz: Class<*>) {

    /**
     * Do Not Disturb flag. If set to true, the notifications are no longer shown.
     */
    var dnd: Boolean = false

    fun notify(notification: ChatNotificationData) {
        if (dnd) return
        startNotificationActivity(notification)
    }

    private fun startNotificationActivity(notification: ChatNotificationData) {
        val intent = Intent(ContextRetainer.context, chatNotificationActivityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra("username", notification.name)
            putExtra("userId", notification.userId)
            putExtra("message", notification.message)
            putExtra("imageUri", notification.imageUri)
            putExtra("participants", notification.usersList.toTypedArray())
        }
        ContextRetainer.context.startActivity(intent)
    }

    companion object {
        const val AUTO_DISMISS_TIME = 3000L
    }
}