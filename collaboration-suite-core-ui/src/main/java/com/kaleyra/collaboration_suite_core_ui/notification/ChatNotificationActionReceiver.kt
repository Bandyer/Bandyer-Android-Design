/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.kaleyra.collaboration_suite.conversation.Chat
import com.kaleyra.collaboration_suite.conversation.Message.Content.Text
import com.kaleyra.collaboration_suite.conversation.Message.State.Received
import com.kaleyra.collaboration_suite_core_ui.CollaborationBroadcastReceiver
import com.kaleyra.collaboration_suite_core_ui.KaleyraVideo
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
        CoroutineScope(Dispatchers.IO).launch {
            val chat = getChat(intent) ?: return@launch
            requestConfigure().let {
                if (!it) {
                    NotificationManager.cancel(chat.id.hashCode())
                    return@let ContextRetainer.context.goToLaunchingActivity()
                }
                when (intent.action) {
                    ACTION_REPLY        -> {
                        val reply = getReply(intent)
                        chat.messages.replayCache[0].other.filter { it.state.value is Received }.forEach { it.markAsRead() }
                        val message = chat.add(Text(reply.toString())).getOrNull()
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

    private suspend fun getChat(intent: Intent): Chat? =
        intent.extras?.getString("chatId")?.let { chatId ->
            KaleyraVideo.conversation.chats.first().firstOrNull { it.id == chatId }
        }

    private fun getReply(intent: Intent): CharSequence? =
        RemoteInput.getResultsFromIntent(intent)?.getCharSequence(ChatNotification.EXTRA_REPLY)
}