package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

@Immutable
data class ConversationState(
    val conversationItems: ImmutableList<ConversationItem>? = null,
    val isFetching: Boolean = false,
    val unreadMessagesCount: Int = 0
)