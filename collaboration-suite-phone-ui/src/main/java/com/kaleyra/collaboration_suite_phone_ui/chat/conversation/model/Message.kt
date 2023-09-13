package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Stable
sealed interface Message {

    val id: String

    val text: String

    val time: String

    data class OtherMessage(
        override val id: String,
        override val text: String,
        override val time: String,
    ) : Message

    data class MyMessage(
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

    companion object {
        fun com.kaleyra.collaboration_suite.conversation.Message.toUiMessage(): Message {
            val text = (content as? com.kaleyra.collaboration_suite.conversation.Message.Content.Text)?.message ?: ""
            val time = TimestampUtils.parseTime(creationDate.time)

            return if (this is com.kaleyra.collaboration_suite.conversation.OtherMessage)
                OtherMessage(id, text, time)
             else
                MyMessage(id, text, time, state.map { state -> mapToUiState(state) })
        }


        private fun mapToUiState(state: com.kaleyra.collaboration_suite.conversation.Message.State): State =
            when (state) {
                is com.kaleyra.collaboration_suite.conversation.Message.State.Sending -> State.Sending
                is com.kaleyra.collaboration_suite.conversation.Message.State.Sent -> State.Sent
                else -> State.Read
            }
    }
}