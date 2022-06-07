package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite_core_ui.ChatBoxUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class ChatNotificationActionReceiver: BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        const val ACTION_REPLY = "com.kaleyra.collaboration_suite_core_ui.ACTION_REPLY"

        const val ACTION_DELETE = "com.kaleyra.collaboration_suite_core_ui.ACTION_DELETE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        when(intent.action) {
            ACTION_REPLY -> {
//                val chatId = intent.extras?.getString("chatId") ?: return
//                val message = intent.extras?.getString(ChatNotification.EXTRA_REPLY) ?: ""
//                CollaborationUI.chatBox.chats.value.first { it.id == chatId }.add()
            }
            else -> Unit
        }
    }
}