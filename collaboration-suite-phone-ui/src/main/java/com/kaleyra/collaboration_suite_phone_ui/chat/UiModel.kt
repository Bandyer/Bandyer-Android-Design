package com.kaleyra.collaboration_suite_phone_ui.chat

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.*

sealed class ChatState {
    sealed class NetworkState : ChatState() {
        object Connecting : NetworkState()
        object Offline : NetworkState()
    }

    sealed class UserState : ChatState() {
        object Online : UserState()
        data class Offline(val timestamp: String?) : UserState()
        object Typing : UserState()
    }

    object None : ChatState()
}

data class ChatInfo(
    val name: String,
    val image: Uri
)

sealed class ChatAction {
    object AudioCall : ChatAction()
    object AudioUpgradableCall : ChatAction()
    object VideoCall : ChatAction()
}

sealed class CallType(val preferredType: Call.PreferredType) {
    object Audio : CallType(Call.PreferredType(video = null))
    object AudioUpgradable : CallType(Call.PreferredType(video = Call.Video.Disabled))
    object Video : CallType(Call.PreferredType())
}

sealed class ConversationItem(val id: String) {
    data class DayItem(val timestamp: String) :
        ConversationItem(id = timestamp.hashCode().toString())

    data class NewMessagesItem(val count: Int) : ConversationItem(id = UUID.randomUUID().toString())
    data class MessageItem(val message: Message, val isMine: Boolean) :
        ConversationItem(id = message.id)
}

data class Message(
    val id: String,
    val text: String,
    val time: String,
    val state: StateFlow<State>
) {
    companion object {
        fun com.kaleyra.collaboration_suite.chatbox.Message.toUiMessage(coroutineScope: CoroutineScope) =
            Message(
                id = id,
                text = (content as? com.kaleyra.collaboration_suite.chatbox.Message.Content.Text)?.message
                    ?: "",
                time = Iso8601.parseTime(creationDate.time),
                state = state.map { state -> mapToUiState(state) }.stateIn(
                    coroutineScope,
                    SharingStarted.Eagerly,
                    mapToUiState(state.value)
                )
            )

        private fun mapToUiState(state: com.kaleyra.collaboration_suite.chatbox.Message.State): State =
            when (state) {
                is com.kaleyra.collaboration_suite.chatbox.Message.State.Sending -> State.Sending
                is com.kaleyra.collaboration_suite.chatbox.Message.State.Sent -> State.Sent
                else -> State.Read
            }
    }

    sealed class State {
        object Sending : State()
        object Sent : State()
        object Read : State()
    }
}
