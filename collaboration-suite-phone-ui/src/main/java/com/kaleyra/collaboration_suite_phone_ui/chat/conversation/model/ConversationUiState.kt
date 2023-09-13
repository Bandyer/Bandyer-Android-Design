package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class ConversationUiState(
    val isFetching: Boolean = false,
    val conversationElements: ImmutableList<ConversationElement>? = null,
    val unreadMessagesCount: Int = 0
)