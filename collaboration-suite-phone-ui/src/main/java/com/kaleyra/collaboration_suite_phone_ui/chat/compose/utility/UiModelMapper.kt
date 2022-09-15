package com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility

import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.Message.Companion.toUiMessage
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
internal object UiModelMapper {
    fun Set<ChatUI.Action>.mapToUiActions(): Set<ChatAction> {
        return mutableSetOf<ChatAction>().apply {
            if (this@mapToUiActions.any { it is ChatUI.Action.CreateCall && !it.preferredType.hasVideo() })
                add(ChatAction.AudioCall)
            if (this@mapToUiActions.any { it is ChatUI.Action.CreateCall && !it.preferredType.isVideoEnabled() })
                add(ChatAction.AudioUpgradableCall)
            if (this@mapToUiActions.any { it is ChatUI.Action.CreateCall && it.preferredType.isVideoEnabled() })
                add(ChatAction.VideoCall)
        }
    }

    fun Flow<PhoneBoxUI>.hasActiveCall(): Flow<Boolean> =
        flatMapLatest { it.call }.flatMapLatest { it.state }.map { it !is Call.State.Disconnected.Ended }

    fun getChatState(participants: Flow<ChatParticipants>, chatBox: ChatBox): Flow<ChatState> {
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

    fun getChatInfo(participants: Flow<ChatParticipants>, usersDescription: Flow<UsersDescription>): Flow<ChatInfo> {
        return combine(participants.otherParticipant(), usersDescription) { participant, usersDesc ->
            ChatInfo(
                name = usersDesc.name(listOf(participant.userId)),
                image = usersDesc.image(listOf(participant.userId))
            )
        }
    }

    fun Flow<ChatUI>.unreadMessagesIds(): Flow<Set<String>> {
        return flatMapLatest { it.messages }
            .map { it.other }
            .drop(1)
            .map { messages -> messages.filter { it.state.value is Message.State.Received }.map { it.id }.toSet() }
    }

    fun Flow<MessagesUI>.mapToConversationItems(coroutineScope: CoroutineScope, showUnreadHeader: StateFlow<Boolean>): Flow<List<ConversationItem>> {
        return combine(this.map { it.list }, this.firstUnreadMessageId()) { messages, firstUnreadMessageId ->
            val items = mutableListOf<ConversationItem>()
            messages.forEachIndexed { index, message ->
                val previousMessage = messages.getOrNull(index + 1)

                items.add(ConversationItem.MessageItem(message.toUiMessage(coroutineScope)))

                if (showUnreadHeader.value && message.id == firstUnreadMessageId) {
                    items.add(ConversationItem.NewMessagesItem(index + 1))
                }

                if (previousMessage == null || !Iso8601.isSameDay(message.creationDate.time, previousMessage.creationDate.time)) {
                    items.add(
                        ConversationItem.DayItem(
                            Iso8601.parseDay(
                                ContextRetainer.context,
                                timestamp = message.creationDate.time
                            )
                        )
                    )
                }
            }
            items
        }
    }

    private fun Flow<MessagesUI>.firstUnreadMessageId(): Flow<String?> {
        return map { it.other }.take(1).map { messages ->
            messages.forEachIndexed { index, message ->
                val previousMessage = messages.getOrNull(index + 1) ?: return@forEachIndexed
                if (message.state.value is Message.State.Received && previousMessage.state.value is Message.State.Read)
                    return@map message.id
            }
            return@map null
        }
    }

    private fun Flow<ChatParticipants>.otherParticipant(): Flow<ChatParticipant> = map { it.others.first() }

    private fun Flow<ChatParticipants>.typingEvents(): Flow<ChatParticipant.Event> = otherParticipant().flatMapLatest { it.events.filterIsInstance<ChatParticipant.Event.Typing>() }

    private fun Flow<ChatParticipants>.otherParticipantState(): Flow<ChatParticipant.State> = otherParticipant().flatMapLatest { it.state }
}

