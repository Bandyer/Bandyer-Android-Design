package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

@Stable
sealed interface Message {

    val userId: String

    val id: String

    val text: String

    val time: String

    data class OtherMessage(
        override val userId: String,
        override val id: String,
        override val text: String,
        override val time: String
    ) : Message

    data class MyMessage(
        override val userId: String,
        override val id: String,
        override val text: String,
        override val time: String,
        val state: Flow<State>
    ) : Message

    @Immutable
    sealed class State {
        object Sending : State()
        object Sent : State()
        object Read : State()
    }
}