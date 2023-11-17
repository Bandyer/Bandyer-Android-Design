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

package com.kaleyra.video_sdk.chat.conversation.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
sealed class ConversationItem(val id: String) {
    data class Day(val timestamp: Long) : ConversationItem(id = timestamp.hashCode().toString())
    object UnreadMessages : ConversationItem(id = UUID.randomUUID().toString())
    data class Message(
        val message: com.kaleyra.video_sdk.chat.conversation.model.Message,
        val isFirstChainMessage: Boolean = true,
        val isLastChainMessage: Boolean = true
    ) : ConversationItem(id = message.id)
}