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

package com.kaleyra.video_sdk.chat.mapper

import com.kaleyra.video.State
import com.kaleyra.video.conversation.Conversation
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object ConversationStateMapper {

    fun Flow<Conversation>.toConnectionState(): Flow<ConnectionState> {
        var previousConversationState: State? = null

        return this
            .flatMapLatest { it.state }
            .map { conversationState ->
                when {
                    conversationState is State.Connecting && previousConversationState is State.Connected -> ConnectionState.Offline
                    conversationState is State.Connecting                                                 -> ConnectionState.Connecting
                    conversationState is State.Connected -> ConnectionState.Connected
                    conversationState is State.Disconnected.Error -> ConnectionState.Error
                    else -> ConnectionState.Unknown
                }.also {
                    previousConversationState = conversationState
                }
            }
    }
}