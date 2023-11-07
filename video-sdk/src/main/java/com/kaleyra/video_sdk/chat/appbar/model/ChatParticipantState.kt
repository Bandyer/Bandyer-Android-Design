package com.kaleyra.video_sdk.chat.appbar.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ChatParticipantState {
    object Online : ChatParticipantState()

    data class Offline(val timestamp: Long? = null) : ChatParticipantState()

    object Typing : ChatParticipantState()

    object Unknown : ChatParticipantState()
}