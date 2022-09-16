package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.chatbox.Message.Content.Text
import com.kaleyra.collaboration_suite.chatbox.Message.State.Received
import com.kaleyra.collaboration_suite_core_ui.CollaborationBroadcastReceiver
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ChatNotificationActionReceiver
 */
class ChatNotificationActionReceiver : CollaborationBroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        const val ACTION_REPLY = "com.kaleyra.collaboration_suite_core_ui.REPLY"
        const val ACTION_MARK_AS_READ = "com.kaleyra.collaboration_suite_core_ui.MARK_AS_READ"
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val chat = getChat(intent) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            requestConfigure().let {
                if (!it) {
                    NotificationManager.cancel(chat.id.hashCode())
                    return@let ContextRetainer.context.goToLaunchingActivity()
                }
                when (intent.action) {
                    ACTION_REPLY        -> {
                        val reply = getReply(intent)
                        val message = chat.create(Text(reply.toString()))
                        chat.messages.replayCache[0].other.filter { it.state.value is Received }.forEach { it.markAsRead() }
                        chat.add(message)
                        NotificationManager.cancel(chat.id.hashCode())
                    }
                    ACTION_MARK_AS_READ -> {
                        chat.messages.replayCache[0].other.filter { it.state.value is Received }.forEach { it.markAsRead() }
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