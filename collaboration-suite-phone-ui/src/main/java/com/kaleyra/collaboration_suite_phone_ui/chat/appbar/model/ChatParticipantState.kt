package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite.conversation.ChatParticipant

@Immutable
sealed class ChatParticipantState {
    object Online : ChatParticipantState()

    data class Offline(val timestamp: Long? = null) : ChatParticipantState()

    object Typing : ChatParticipantState()

    object Unknown : ChatParticipantState()
}