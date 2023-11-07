package com.kaleyra.video_sdk.chat.conversation.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

@Stable
sealed interface Message {

    val id: String

    val content: String

    val time: String

    data class OtherMessage(
        override val id: String,
        override val content: String,
        override val time: String,
        val userId: String
    ) : Message

    data class MyMessage(
        override val id: String,
        override val content: String,
        override val time: String,
        val state: Flow<State>
    ) : Message

    @Immutable
    sealed class State {
        object Created : State()
        object Received : State()
        object Sending : State()
        object Sent : State()
        object Read : State()
    }
}