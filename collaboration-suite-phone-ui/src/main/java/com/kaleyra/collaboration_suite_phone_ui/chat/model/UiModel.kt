package com.kaleyra.collaboration_suite_phone_ui.chat.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

@Immutable
data class ChatUiState(
    val info: ChatInfo = ChatInfo("", null),
    val state: ChatState = ChatState.None,
    val actions: ImmutableSet<ChatAction> = ImmutableSet(setOf()),
    val conversationState: ConversationUiState = ConversationUiState(),
    val isInCall: Boolean = false
)

@Immutable
data class ConversationUiState(
    val isFetching: Boolean = false,
    val conversationItems: ImmutableList<ConversationItem>? = null,
    val unreadMessagesCount: Int = 0
)

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

@Immutable
sealed class ConversationItem(val id: String) {
    data class DayItem(val timestamp: Long) : ConversationItem(id = timestamp.hashCode().toString())
    object UnreadMessagesItem : ConversationItem(id = UUID.randomUUID().toString())
    data class MessageItem(val message: Message) : ConversationItem(id = message.id)
}

@Immutable
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
        fun com.kaleyra.collaboration_suite.chatbox.Message.toUiMessage(): Message {
            val text = (content as? com.kaleyra.collaboration_suite.chatbox.Message.Content.Text)?.message ?: ""
            val time = TimestampUtils.parseTime(creationDate.time)

            return if (this is com.kaleyra.collaboration_suite.chatbox.OtherMessage)
                OtherMessage(id, text, time)
             else
                MyMessage(id, text, time, state.map { state -> mapToUiState(state) })
        }


        private fun mapToUiState(state: com.kaleyra.collaboration_suite.chatbox.Message.State): State =
            when (state) {
                is com.kaleyra.collaboration_suite.chatbox.Message.State.Sending -> State.Sending
                is com.kaleyra.collaboration_suite.chatbox.Message.State.Sent -> State.Sent
                else -> State.Read
            }
    }
}

// Needed for compose stability to avoid recomposition
// Tried kotlinx-collections-immutable but they were not working properly
@Immutable
data class ImmutableList<out T>(val value: List<T>) {
    fun count() = value.count()
}

@Immutable
data class ImmutableSet<out T>(val value: Set<T>) {
    fun count() = value.count()
}
