package com.kaleyra.video_sdk.chat.conversation.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
data class ConversationState(
    val conversationItems: ImmutableList<ConversationItem>? = null,
    val isFetching: Boolean = false,
    val unreadMessagesCount: Int = 0
)