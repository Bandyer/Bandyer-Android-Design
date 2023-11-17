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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

open class ChatViewModel(configure: suspend () -> Configuration) : CollaborationViewModel(configure) {

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)
    val chat = _chat.asSharedFlow()

    val call = conference.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    val messages = chat.flatMapLatest { it.messages }.shareInEagerly(viewModelScope)

    val actions = chat.flatMapLatest { it.actions }.shareInEagerly(viewModelScope)

    val participants = chat.flatMapLatest { it.participants }.shareInEagerly(viewModelScope)

    suspend fun setChat(userId: String): ChatUI? {
        val conversation = conversation.first()
        val chat = conversation.create(userId).getOrNull() ?: return null
        _chat.tryEmit(chat)
        return chat
    }

    suspend fun setChat(userIds: List<String>, chatId: String): ChatUI? {
        val conversation = conversation.first()
        val chat = conversation.create(userIds, chatId).getOrNull() ?: return null
        _chat.tryEmit(chat)
        return chat
    }
}
