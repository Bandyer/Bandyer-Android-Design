package com.kaleyra.collaboration_suite_phone_ui.chat.screen.model

import androidx.compose.runtime.Stable
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.common.uistate.UiState

@Stable
sealed interface ChatUiState : UiState {

    val actions: ImmutableSet<ChatAction>

    val connectionState: ConnectionState

    val conversationState: ConversationState

    val isInCall: Boolean

    data class OneToOne(
        val recipientDetails: ParticipantDetails = ParticipantDetails("", ImmutableUri()),
        override val actions: ImmutableSet<ChatAction> = ImmutableSet(),
        override val connectionState: ConnectionState,
        override val conversationState: ConversationState = ConversationState(),
        override val isInCall: Boolean = false
    ): ChatUiState

    data class Group(
        val name: String = "",
        val image: ImmutableUri = ImmutableUri(),
        val participantsDetails: ImmutableMap<String, ParticipantDetails> = ImmutableMap(),
        override val actions: ImmutableSet<ChatAction> = ImmutableSet(),
        override val connectionState: ConnectionState,
        override val conversationState: ConversationState = ConversationState(),
        override val isInCall: Boolean = false
    ): ChatUiState
}