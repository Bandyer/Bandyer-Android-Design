package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.Intent
import com.kaleyra.collaboration_suite_utils.ContextRetainer

object CustomChatNotificationManager {

    const val AUTO_DISMISS_TIME = 3000L

    fun notify(message: ChatNotificationMessage, chatId: String, nOfMessages: Int, chatNotificationActivityClazz: Class<*>) =
        startNotificationActivity(message, chatId, nOfMessages, chatNotificationActivityClazz)

    private fun startNotificationActivity(message: ChatNotificationMessage, chatId: String, nOfMessages: Int, chatNotificationActivityClazz: Class<*>) {
        val intent = Intent(ContextRetainer.context, chatNotificationActivityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra("userId", message.userId)
            putExtra("username", message.username)
            putExtra("avatar", message.avatar)
            putExtra("message", message.text)
            putExtra("timestamp", message.timestamp)
            putExtra("chatId", chatId)
            putExtra("nOfMessages", nOfMessages)
        }
        ContextRetainer.context.startActivity(intent)
    }
}