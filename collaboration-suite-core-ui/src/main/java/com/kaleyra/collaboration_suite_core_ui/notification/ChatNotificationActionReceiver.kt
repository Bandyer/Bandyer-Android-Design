package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.chatbox.Message.Content.Text
import com.kaleyra.collaboration_suite.chatbox.Message.State.Received
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.whenCollaborationConfigured
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ChatNotificationActionReceiver
 */
internal class ChatNotificationActionReceiver : BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        const val ACTION_REPLY = "com.kaleyra.collaboration_suite_core_ui.ACTION_REPLY"
        const val ACTION_MARK_AS_READ = "com.kaleyra.collaboration_suite_core_ui.ACTION_MARK_AS_READ"
        const val ACTION_DELETE = "com.kaleyra.collaboration_suite_core_ui.ACTION_DELETE"
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val chat = getChat(intent) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            whenCollaborationConfigured {
                if (!it) return@whenCollaborationConfigured NotificationManager.cancel(chat.id.hashCode())
                when (intent.action) {
                    ACTION_REPLY        -> {
                        val reply = getReply(intent)
                        val message = chat.create(Text(reply.toString()))
                        chat.messages.value.other.filter { it.state.value is Received }.forEach { it.markAsRead() }
                        chat.add(message)
                        NotificationManager.cancel(chat.id.hashCode())
                    }
                    ACTION_MARK_AS_READ -> {
                        chat.messages.value.other.filter { it.state.value is Received }.forEach { it.markAsRead() }
                        NotificationManager.cancel(chat.id.hashCode())
                    }
                    else                -> Unit
                }
            }
            pendingResult.finish()
        }
    }

    private fun getChat(intent: Intent): Chat? =
        intent.extras?.getString("chatId")?.let { chatId ->
            CollaborationUI.chatBox.chats.value.firstOrNull { it.id == chatId }
        }

    private fun getReply(intent: Intent): CharSequence? =
        RemoteInput.getResultsFromIntent(intent)?.getCharSequence(ChatNotification.EXTRA_REPLY)
}