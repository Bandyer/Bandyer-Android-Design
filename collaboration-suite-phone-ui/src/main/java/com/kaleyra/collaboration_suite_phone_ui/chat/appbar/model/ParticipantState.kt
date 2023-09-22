package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Stable
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

@Stable
sealed interface ParticipantState {
    data class Online(val username: ImmutableList<String>) : ParticipantState

    data class Offline(val username: String, val timestamp: Long? = null) : ParticipantState

    data class Typing(val username: ImmutableList<String>) : ParticipantState
}