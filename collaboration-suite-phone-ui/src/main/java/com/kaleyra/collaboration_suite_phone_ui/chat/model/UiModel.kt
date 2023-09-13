package com.kaleyra.collaboration_suite_phone_ui.chat.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

@Immutable
sealed class ChatState {
    sealed class NetworkState : ChatState() {
        object Connecting : NetworkState()
        object Offline : NetworkState()
    }

    sealed class UserState : ChatState() {
        object Online : UserState()
        data class Offline(val timestamp: Long?) : UserState()
        object Typing : UserState()
    }

    object None : ChatState()
}

// Image is nullable for testing purpose. It is not possible
// to mock a static field, since it has no getter.
@Immutable
data class ChatInfo(
    val name: String = "",
    val image: ImmutableUri? = null
)

@Immutable
sealed class ChatAction(open val onClick: () -> Unit) {
    data class AudioCall(override val onClick: () -> Unit) : ChatAction(onClick)
    data class AudioUpgradableCall(override val onClick: () -> Unit) : ChatAction(onClick)
    data class VideoCall(override val onClick: () -> Unit) : ChatAction(onClick)
}

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

// Needed for compose stability to avoid recomposition
// Tried kotlinx-collections-immutable but they were not working properly
@Immutable
data class ImmutableList<out T>(val value: List<T>) {
    fun getOrNull(index: Int) = value.getOrNull(index)
    fun count() = value.count()
}

@Immutable
data class ImmutableSet<out T>(val value: Set<T>) {
    fun count() = value.count()
}
