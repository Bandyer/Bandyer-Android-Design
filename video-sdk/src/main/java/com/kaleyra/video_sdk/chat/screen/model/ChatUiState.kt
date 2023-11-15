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