package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
sealed class ConversationElement(val id: String) {
    data class Day(val timestamp: Long) : ConversationElement(id = timestamp.hashCode().toString())
    data object UnreadMessages : ConversationElement(id = UUID.randomUUID().toString())
    data class Message(val data: com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message) : ConversationElement(id = data.id)
}