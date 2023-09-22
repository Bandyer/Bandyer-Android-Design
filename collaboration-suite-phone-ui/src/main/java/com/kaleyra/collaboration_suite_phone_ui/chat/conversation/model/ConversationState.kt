package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap

@Immutable
data class ConversationState(
    val conversationItems: ImmutableList<ConversationItem>? = null,
    val participantsDetails: ImmutableMap<String, ParticipantDetails> = ImmutableMap(),
    val isFetching: Boolean = false,
    val unreadMessagesCount: Int = 0
)