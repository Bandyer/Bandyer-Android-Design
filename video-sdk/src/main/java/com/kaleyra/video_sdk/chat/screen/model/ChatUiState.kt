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

package com.kaleyra.video_sdk.chat.screen.model

import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantsState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.conversation.model.ConversationState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.common.uistate.UiState
import kotlinx.coroutines.flow.flowOf

@Stable
internal sealed interface ChatUiState : UiState {

    val actions: ImmutableSet<ChatAction>

    val connectionState: ConnectionState

    val conversationState: ConversationState

    val isInCall: Boolean

    val isUserConnected: Boolean

    data class OneToOne(
        val recipientDetails: ChatParticipantDetails = ChatParticipantDetails("", ImmutableUri(), flowOf()),
        override val actions: ImmutableSet<ChatAction> = ImmutableSet(),
        override val connectionState: ConnectionState = ConnectionState.Unknown,
        override val conversationState: ConversationState = ConversationState(),
        override val isInCall: Boolean = false,
        override val isUserConnected: Boolean = true
    ): ChatUiState

    data class Group(
        val name: String = "",
        val image: ImmutableUri = ImmutableUri(),
        val participantsDetails: ImmutableMap<String, ChatParticipantDetails> = ImmutableMap(),
        val participantsState: ChatParticipantsState = ChatParticipantsState(),
        override val actions: ImmutableSet<ChatAction> = ImmutableSet(),
        override val connectionState: ConnectionState = ConnectionState.Unknown,
        override val conversationState: ConversationState = ConversationState(),
        override val isInCall: Boolean = false,
        override val isUserConnected: Boolean = true
    ): ChatUiState
}