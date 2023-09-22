package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ParticipantState {
    object Online : ParticipantState()

    data class Offline(val timestamp: Long? = null) : ParticipantState()

    object Typing : ParticipantState()
}