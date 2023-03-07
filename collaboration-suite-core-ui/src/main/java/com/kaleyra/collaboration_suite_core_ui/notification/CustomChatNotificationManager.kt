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

import android.content.Intent
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * The custom chat notification manager
 */
object CustomChatNotificationManager {

    /**
     * Send a custom notification
     *
     * @param chatId The chat id
     * @param chatNotificationActivityClazz The chat notification activity Class<*>
     */
    internal fun notify(chatId: String, chatNotificationActivityClazz: Class<*>) {
        if (chatId == DisplayedChatActivity.chatId.value) return
        startNotificationActivity(chatId, chatNotificationActivityClazz)
    }

    private fun startNotificationActivity(chatId: String, chatNotificationActivityClazz: Class<*>) {
        val intent = Intent(ContextRetainer.context, chatNotificationActivityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra("chatId", chatId)
        }
        ContextRetainer.context.startActivity(intent)
    }

}