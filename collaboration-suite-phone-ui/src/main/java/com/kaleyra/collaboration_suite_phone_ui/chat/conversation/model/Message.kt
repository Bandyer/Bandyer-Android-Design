package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow

@Stable
sealed interface Message {

    val id: String

    val text: String

    val time: String

    data class OtherMessage(
        override val id: String,
        override val text: String,
        override val time: String
    ) : Message

    data class MyMessage(
        override val id: String,
        override val text: String,
        override val time: String,
    ) : Message

    @Immutable
    sealed class State {
        object Sending : State()
        object Sent : State()
        object Read : State()
    }
}