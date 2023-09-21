package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
sealed class ConversationElement(val id: String) {
    data class Day(val timestamp: Long) : ConversationElement(id = timestamp.hashCode().toString())
    object UnreadMessages : ConversationElement(id = UUID.randomUUID().toString())
    data class Message(val message: com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message, val isLastChainMessage: Boolean = true) : ConversationElement(id = message.id)
}