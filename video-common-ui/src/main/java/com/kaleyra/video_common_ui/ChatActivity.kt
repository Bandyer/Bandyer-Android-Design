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

package com.kaleyra.video_common_ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video_common_ui.notification.DisplayedChatActivity
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class ChatActivity : FragmentActivity() {

    protected abstract val viewModel: ChatViewModel

    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChatOrCloseActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setChatOrCloseActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        chatId?.let { sendChatAction(DisplayedChatActivity.ACTION_CHAT_VISIBLE, it) }
    }

    override fun onStop() {
        super.onStop()
        sendChatAction(DisplayedChatActivity.ACTION_CHAT_NOT_VISIBLE)
    }

    private fun setChatOrCloseActivity(intent: Intent) = lifecycleScope.launch {
        when {
            !viewModel.isCollaborationConfigured.first() -> ContextRetainer.context.goToLaunchingActivity()
            setChat(intent) -> return@launch
        }
        finishAndRemoveTask()
    }

    private suspend fun setChat(intent: Intent): Boolean {
        val userIds = intent.extras?.getStringArray("userIds") ?: return false
        val chatId = intent.extras?.getString("chatId")
        if (userIds.size > 1 && chatId == null) return false
        val chat = if (userIds.size > 1) viewModel.setChat(userIds.toList(), chatId!!) ?: return false
        else viewModel.setChat(userIds.first()) ?: return false
        this@ChatActivity.chatId = chat.id
        sendChatAction(DisplayedChatActivity.ACTION_CHAT_VISIBLE, chat.id)
        return true
    }

    private fun sendChatAction(action: String, chatId: String? = null) {
        sendBroadcast(Intent(this, DisplayedChatActivity::class.java).apply {
            this.action = action
            chatId?.let { putExtra(DisplayedChatActivity.EXTRA_CHAT_ID, it) }
        })
    }
}