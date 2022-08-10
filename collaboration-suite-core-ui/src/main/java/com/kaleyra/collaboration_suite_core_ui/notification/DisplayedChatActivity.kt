package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Utility class to know the chat's id of the chat displayed in the chat activity
 */
class DisplayedChatActivity internal constructor(): BroadcastReceiver() {
    /**
     * @suppress
     */
    companion object {
        const val ACTION_CHAT_VISIBLE = "com.kaleyra.collaboration_suite_core_ui.CHAT_OPEN"

        const val ACTION_CHAT_NOT_VISIBLE = "com.kaleyra.collaboration_suite_core_ui.CHAT_CLOSE"

        const val EXTRA_CHAT_ID = "chatId"

        private val _chatId: MutableStateFlow<String?> = MutableStateFlow(null)
        internal val chatId: StateFlow<String?> = _chatId
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CHAT_VISIBLE -> _chatId.value = intent.extras?.getString(EXTRA_CHAT_ID, null)
            ACTION_CHAT_NOT_VISIBLE -> _chatId.value = null
            else -> Unit
        }
    }
}