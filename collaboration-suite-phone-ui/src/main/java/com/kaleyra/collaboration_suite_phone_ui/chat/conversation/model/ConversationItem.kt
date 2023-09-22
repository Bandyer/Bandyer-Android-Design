package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
sealed class ConversationItem(val id: String) {
    data class Day(val timestamp: Long) : ConversationItem(id = timestamp.hashCode().toString())
    object UnreadMessages : ConversationItem(id = UUID.randomUUID().toString())
    data class Message(
        val message: com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message,
        val isFirstChainMessage: Boolean = true,
        val isLastChainMessage: Boolean = true
    ) : ConversationItem(id = message.id)
}