package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.RemoteInput
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

        const val ACTION_DELETE = "com.kaleyra.collaboration_suite_core_ui.ACTION_DELETE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        when(intent.action) {
            ACTION_REPLY -> {
                val chatId = intent.extras?.getString("chatId") ?: return
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
                val chat = CollaborationUI.chatBox.chats.value.first { it.id == chatId }
                chat.messages.value.other.forEach { it.markAsRead() }
                chat.add(message)
                NotificationManager.cancelNotification(chatId.hashCode())
            }
            else -> Unit
        }
    }

    private fun getReply(intent: Intent): CharSequence? =
        RemoteInput.getResultsFromIntent(intent)?.getCharSequence(ChatNotification.EXTRA_REPLY)
}