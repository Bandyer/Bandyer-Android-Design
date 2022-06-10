package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class ChatNotificationActionReceiver: BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        const val ACTION_REPLY = "com.kaleyra.collaboration_suite_core_ui.ACTION_REPLY"
        const val ACTION_MARK_AS_READ = "com.kaleyra.collaboration_suite_core_ui.ACTION_MARK_AS_READ"
        const val ACTION_DELETE = "com.kaleyra.collaboration_suite_core_ui.ACTION_DELETE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        when(intent.action) {
            ACTION_REPLY -> {
                val reply = getReply(intent)
                val participant = object : ChatParticipant {
                    override val state: StateFlow<ChatParticipant.State> = MutableStateFlow(ChatParticipant.State.Joined.Online)
                    override val events: StateFlow<ChatParticipant.Event> = MutableStateFlow(ChatParticipant.Event.Typing.Idle)
                    override val userId: String = ""

                }
                val message = object : Message {
                    override val id = UUID.randomUUID().toString()
                    override val creator: ChatParticipant = participant
                    override val creationDate = Date()
                    override val content = Message.Content.Text(reply.toString())
                    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Created())
                }
                val chat = getChat(intent) ?: return
                chat.messages.value.other.filter { it.state.value is Message.State.Received }.forEach { it.markAsRead() }
                chat.add(message)
                NotificationManager.cancel(chat.id.hashCode())
            }
            ACTION_MARK_AS_READ -> {
                val chat = getChat(intent) ?: return
                chat.messages.value.other.filter { it.state.value is Message.State.Received }.forEach { it.markAsRead() }
                NotificationManager.cancel(chat.id.hashCode())
            }
            else -> Unit
        }
    }

    private fun getChat(intent: Intent): Chat? =
        intent.extras?.getString("chatId")?.let { chatId ->
            CollaborationUI.chatBox.chats.value.firstOrNull { it.id == chatId }
        }

    private fun getReply(intent: Intent): CharSequence? =
        RemoteInput.getResultsFromIntent(intent)?.getCharSequence(ChatNotification.EXTRA_REPLY)
}