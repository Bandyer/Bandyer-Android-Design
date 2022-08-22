@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_phone_ui.chat.Message.Companion.toUiMessage
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update

internal class PhoneChatViewModel : ChatViewModel(), ChatUiViewModel {

    private val _uiState = MutableStateFlow(ChatUiState())

    override val uiState: StateFlow<ChatUiState>
        get() = _uiState

    private val showUnreadHeader = MutableStateFlow(true)

    private val unseenMessagesIds = MutableStateFlow<Set<String>>(setOf()).also { flow ->
        chat.unreadMessagesIds()
            .onEach { messages -> flow.value += messages.toSet() }
            .launchIn(viewModelScope)
    }

    init {
        getChatState(participants, chatBox.replayCache.first()).onEach { state ->
            _uiState.update { it.copy(state = state) }
        }.launchIn(viewModelScope)

        getChatInfo(participants, usersDescription).onEach { info ->
            _uiState.update { it.copy(info = info) }
        }.launchIn(viewModelScope)

        actions.map { it.mapToUiActions() }.onEach { actions ->
            _uiState.update { it.copy(actions = actions) }
        }.launchIn(viewModelScope)

        phoneBox.hasActiveCall().onEach { hasActiveCall ->
            _uiState.update { it.copy(isInCall = hasActiveCall) }
        }.launchIn(viewModelScope)

        messages.mapToConversationItems(viewModelScope, showUnreadHeader).onEach { items ->
            _uiState.update { it.copy(conversationItems = items) }
        }.launchIn(viewModelScope)

        messages.take(1).map { true }.onEach { areMessagesFetched ->
            _uiState.update { it.copy(areMessagesFetched = areMessagesFetched) }
        }.launchIn(viewModelScope)

        unseenMessagesIds.onEach { messages ->
            _uiState.update { it.copy(unseenMessagesCount = messages.count()) }
        }.launchIn(viewModelScope)
    }

    override fun readAllMessages() {
        val messages = messages.replayCache.firstOrNull() ?: return
        messages.other.forEach { it.markAsRead() }
    }

    override fun sendMessage(text: String) {
        val chat = chat.replayCache.firstOrNull() ?: return
        val message = chat.create(Message.Content.Text(text))
        chat.add(message)
        showUnreadHeader.value = false
    }

    override fun fetchMessages() {
        chat.replayCache.firstOrNull()?.fetch(FETCH_COUNT)
    }

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        unseenMessagesIds.value = unseenMessagesIds.value - messageItem.id
    }

    override fun onAllMessagesScrolled() {
        unseenMessagesIds.value = setOf()
    }

    override fun call(callType: CallType) {
        val phoneBox = phoneBox.replayCache.firstOrNull() ?: return
        val chat = chat.replayCache.firstOrNull() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User {
            override val userId = userId
        })) {
            preferredType = callType.preferredType
        }
    }

    override fun showCall() = CollaborationUI.phoneBox.showCall()

    companion object {
        private const val FETCH_COUNT = 50
    }
}

private fun Set<ChatUI.Action>.mapToUiActions(): Set<ChatAction> {
    return mutableSetOf<ChatAction>().apply {
        if (this@mapToUiActions.any { it is ChatUI.Action.CreateCall && !it.preferredType.hasVideo() })
            add(ChatAction.AudioCall)
        if (this@mapToUiActions.any { it is ChatUI.Action.CreateCall && !it.preferredType.isVideoEnabled() })
            add(ChatAction.AudioUpgradableCall)
        if (this@mapToUiActions.any { it is ChatUI.Action.CreateCall && it.preferredType.isVideoEnabled() })
            add(ChatAction.VideoCall)
    }
}

private fun Flow<PhoneBoxUI>.hasActiveCall(): Flow<Boolean> =
    flatMapLatest { it.call }.flatMapLatest { it.state }.map { it !is Call.State.Disconnected.Ended }

private fun Flow<MessagesUI>.firstUnreadMessageId(): Flow<String?> {
    return map { it.other }.take(1).map { messages ->
        messages.forEachIndexed { index, message ->
            val previousMessage = messages.getOrNull(index + 1)
            if (previousMessage == null || (message.state.value is Message.State.Received && previousMessage.state.value is Message.State.Read))
                return@map message.id
        }
        return@map null
    }
}

private fun Flow<ChatParticipants>.otherParticipant(): Flow<ChatParticipant> = map { it.others.first() }

private fun Flow<ChatParticipants>.typingEvents(): Flow<ChatParticipant.Event> = otherParticipant().flatMapLatest { it.events.filterIsInstance<ChatParticipant.Event.Typing>() }

private fun Flow<ChatParticipants>.otherParticipantState(): Flow<ChatParticipant.State> = otherParticipant().flatMapLatest { it.state }

private fun getChatState(participants: Flow<ChatParticipants>, chatBox: ChatBox): Flow<ChatState> {
    var previousChatBoxState: ChatBox.State? = null

    return combine(
        participants.typingEvents(),
        chatBox.state,
        participants.otherParticipantState()
    ) { event, chatBoxState, participantState ->
        when {
            chatBoxState is ChatBox.State.Connecting && previousChatBoxState is ChatBox.State.Connected -> ChatState.NetworkState.Offline
            chatBoxState is ChatBox.State.Connecting -> ChatState.NetworkState.Connecting
            event is ChatParticipant.Event.Typing.Idle && participantState is ChatParticipant.State.Joined.Online -> ChatState.UserState.Online
            event is ChatParticipant.Event.Typing.Idle && participantState is ChatParticipant.State.Joined.Offline -> {
                val lastLogin = participantState.lastLogin
                ChatState.UserState.Offline(
                    if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At)
                        Iso8601.parseTimestamp(ContextRetainer.context, lastLogin.date.time)
                    else null
                )
            }
            event is ChatParticipant.Event.Typing.Started -> ChatState.UserState.Typing
            else -> ChatState.None
        }.also {
            previousChatBoxState = chatBoxState
        }
    }
}

private fun getChatInfo(participants: Flow<ChatParticipants>, usersDescription: Flow<UsersDescription>): Flow<ChatInfo> {
    return combine(participants.otherParticipant(), usersDescription) { participant, usersDesc ->
        ChatInfo(
            name = usersDesc.name(listOf(participant.userId)),
            image = usersDesc.image(listOf(participant.userId))
        )
    }
}

private fun Flow<ChatUI>.unreadMessagesIds(): Flow<Set<String>> {
    return flatMapLatest { it.messages }
        .map { it.other }
        .drop(1)
        .map { messages -> messages.filter { it.state.value is Message.State.Received }.map { it.id }.toSet() }
}

private fun Flow<MessagesUI>.mapToConversationItems(coroutineScope: CoroutineScope, showUnreadHeader: StateFlow<Boolean>): Flow<List<ConversationItem>> {
    return combine(this.map { it.list }, this.firstUnreadMessageId()) { messages, firstUnreadMessageId ->
        val items = mutableListOf<ConversationItem>()
        messages.forEachIndexed { index, message ->
            val previousMessage = messages.getOrNull(index + 1)

            items.add(ConversationItem.MessageItem(message.toUiMessage(coroutineScope), message !is OtherMessage))

            if (showUnreadHeader.value && message.id == firstUnreadMessageId) {
                items.add(ConversationItem.NewMessagesItem(index + 1))
            }

            if (previousMessage == null || !Iso8601.isSameDay(message.creationDate.time, previousMessage.creationDate.time)) {
                items.add(ConversationItem.DayItem(Iso8601.parseDay(ContextRetainer.context, timestamp = message.creationDate.time)))
            }
        }
        items
    }
}





