@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_phone_ui.chat.Message.Companion.toUiMessage
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

internal class PhoneChatViewModel: ChatViewModel(), ChatComposeViewModel {

    companion object {
        private const val FETCH_COUNT = 50
    }

    private val _firstUnreadMessageId = messages.map { it.other }.take(1).map { messages ->
        messages.forEachIndexed { index, message ->
            val previousMessage = messages.getOrNull(index - 1) ?: return@forEachIndexed
            if (previousMessage.state.value is Message.State.Received && message.state.value is Message.State.Read)
                return@map previousMessage.id
        }
        return@map null
    }

    private val _showUnreadHeader = MutableStateFlow(true)

    private val _unseenMessages = MutableStateFlow<Set<String>>(setOf()).also { flow ->
        chat
            .flatMapLatest { it.messages }
            .map { it.other }
            .drop(1)
            .onEach { messages ->
                val receivedMessages =
                    messages.filter { it.state.value is Message.State.Received }.map { it.id }
                flow.value = flow.value + receivedMessages.toSet()
            }.launchIn(viewModelScope)
    }

    private val _otherParticipant = participants.map { it.others.first() }

    private val _typingEvents = _otherParticipant.flatMapLatest { it.events.filterIsInstance<ChatParticipant.Event.Typing>() }

    private val _otherParticipantState = _otherParticipant.flatMapLatest { it.state }

    private var previousChatBoxState: ChatBox.State? = null

    private val state = combine(
        _typingEvents,
        chatBoxState,
        _otherParticipantState
    ) { event, chatBoxState, participantState ->
        when {
            chatBoxState is ChatBox.State.Connecting && previousChatBoxState is ChatBox.State.Connected -> State.NetworkState.Offline
            chatBoxState is ChatBox.State.Connecting -> State.NetworkState.Connecting
            event is ChatParticipant.Event.Typing.Idle && participantState is ChatParticipant.State.Joined.Online -> State.UserState.Online
            event is ChatParticipant.Event.Typing.Idle && participantState is ChatParticipant.State.Joined.Offline -> {
                val lastLogin = participantState.lastLogin
                State.UserState.Offline(
                    if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At)
                        Iso8601.parseTimestamp(ContextRetainer.context, lastLogin.date.time)
                    else null
                )
            }
            event is ChatParticipant.Event.Typing.Started -> State.UserState.Typing
            else -> State.None
        }.also {
            previousChatBoxState = chatBoxState
        }
    }

    private val info = combine(participants, usersDescription) { participants, usersDescription ->
        val otherUserId = participants.others.first().userId
        Info(
            title = usersDescription.name(listOf(otherUserId)),
            image = usersDescription.image(listOf(otherUserId))
        )
    }

    override val stateInfo = combine(state, info) { state, info ->
        StateInfo(
            state,
            info
        )
    }

    override val chatActions = actions.map { actions ->
        mutableSetOf<Action>().apply {
            if (actions.any { it is ChatUI.Action.CreateCall && !it.preferredType.hasVideo() })
                add(Action.AudioCall)
            if (actions.any { it is ChatUI.Action.CreateCall && !it.preferredType.isVideoEnabled() })
                add(Action.AudioUpgradableCall)
            if (actions.any { it is ChatUI.Action.CreateCall && it.preferredType.isVideoEnabled() })
                add(Action.VideoCall)
        }
    }

    override val conversationItems =
        combine(messages.map { it.list }, _firstUnreadMessageId) { messages, firstUnreadMessageId ->
            val items = mutableListOf<ConversationItem>()
            messages.forEachIndexed { index, message ->
                val previousMessageItem = messages.getOrNull(index - 1) ?: kotlin.run {
                    items.add(ConversationItem.MessageItem(toUiMessage(viewModelScope, message), message !is OtherMessage))
                    return@forEachIndexed
                }

                if (_showUnreadHeader.value && previousMessageItem.id == firstUnreadMessageId) {
                    items.add(ConversationItem.NewMessagesItem(index))
                }

                if (!Iso8601.isSameDay(message.creationDate.time, previousMessageItem.creationDate.time)) {
                    items.add(
                        ConversationItem.DayItem(
                            Iso8601.parseDay(
                                ContextRetainer.context,
                                timestamp = previousMessageItem.creationDate.time
                            )
                        )
                    )
                }

                items.add(ConversationItem.MessageItem(toUiMessage(viewModelScope, message), message !is OtherMessage))
            }
            items
        }

    override val unseenMessagesCount = _unseenMessages.map { it.count() }

    override val isCallActive = phoneBox
        .flatMapLatest { it.call }
        .flatMapLatest { it.state }
        .map { it !is Call.State.Disconnected.Ended }

    override val areMessagesFetched = messages.take(1).map { true }

    override fun readAllMessages() {
        val messages = messages.replayCache.firstOrNull() ?: return
        messages.other.forEach { it.markAsRead() }
    }

    override fun sendMessage(text: String) {
        val chat = chat.replayCache.firstOrNull() ?: return
        val message = chat.create(Message.Content.Text(text))
        chat.add(message)
        _showUnreadHeader.value = false
    }

    override fun fetchMessages() {
        chat.replayCache.firstOrNull()?.fetch(FETCH_COUNT)
    }

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        _unseenMessages.value = _unseenMessages.value - messageItem.id
    }

    override fun onAllMessagesScrolled() {
        _unseenMessages.value = setOf()
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
}