package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * The custom chat notification manager
 */
class CustomChatNotificationManager internal constructor(): BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        const val ACTION_CHAT_OPEN = "com.kaleyra.collaboration_suite_core_ui.CHAT_OPEN"

        const val ACTION_CHAT_CLOSE = "com.kaleyra.collaboration_suite_core_ui.CHAT_CLOSE"

        const val EXTRA_CHAT_ID = "chatId"

        private var chatActivityChatId = ""

        /**
         * Send a custom notification
         *
         * @param chatId The chat id
         * @param chatNotificationActivityClazz The chat notification activity Class<*>
         */
        internal fun notify(chatId: String, chatNotificationActivityClazz: Class<*>) {
            if (chatId == chatActivityChatId) return
            startNotificationActivity(chatId, chatNotificationActivityClazz)
        }

        private fun startNotificationActivity(chatId: String, chatNotificationActivityClazz: Class<*>) {
            val intent = Intent(ContextRetainer.context, chatNotificationActivityClazz).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                putExtra("chatId", chatId)
            }
            ContextRetainer.context.startActivity(intent)
        }
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_CHAT_OPEN -> chatActivityChatId = intent.extras?.getString(EXTRA_CHAT_ID, "") ?: ""
            ACTION_CHAT_CLOSE -> chatActivityChatId = ""
            else -> Unit
        }
    }
}