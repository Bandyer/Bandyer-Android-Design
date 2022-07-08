package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant.Event
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant.Event.Typing.Idle
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant.State
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant.State.Joined.Online
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.Message.Content.Text
import com.kaleyra.collaboration_suite.chatbox.Message.State.Created
import com.kaleyra.collaboration_suite.chatbox.Message.State.Received
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.whenCollaborationConfigured
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

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
                        val participant = object : ChatParticipant {
                            override val state: StateFlow<State> = MutableStateFlow(Online)
                            override val events: StateFlow<Event> = MutableStateFlow(Idle)
                            override val userId: String = ""
                        }
                        val message = object : Message {
                            override val id = UUID.randomUUID().toString()
                            override val creator: ChatParticipant = participant
                            override val creationDate = Date()
                            override val content = Text(reply.toString())
                            override val state: StateFlow<Message.State> = MutableStateFlow(Created())
                        }
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