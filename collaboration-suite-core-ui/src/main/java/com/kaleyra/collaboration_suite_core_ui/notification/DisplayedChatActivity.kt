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