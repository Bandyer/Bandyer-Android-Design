package com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility

import com.kaleyra.collaboration_suite.chatbox.*
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
internal object UiModelMapper {
    fun Set<ChatUI.Action>.mapToChatActions(call: (Call.PreferredType) -> Unit): Set<ChatAction> {
        return mutableSetOf<ChatAction>().apply {
            val actions = this@mapToChatActions.filterIsInstance<ChatUI.Action.CreateCall>()
            actions.firstOrNull { !it.preferredType.hasVideo() }?.also { action ->
                add(ChatAction.AudioCall { call(action.preferredType) })
            }
            actions.firstOrNull { !it.preferredType.isVideoEnabled() }?.also { action ->
                add(ChatAction.AudioUpgradableCall { call(action.preferredType) })
            }
            actions.firstOrNull { it.preferredType.isVideoEnabled() }?.also { action ->
                add(ChatAction.VideoCall { call(action.preferredType) })
            }
        }
    }

    fun Flow<PhoneBoxUI>.hasActiveCall(): Flow<Boolean> =
        flatMapLatest { it.call }.flatMapLatest { it.state }.map { it !is Call.State.Disconnected.Ended }

    fun getChatState(
        participants: Flow<ChatParticipants>,
        chatBox: Flow<ChatBox>
    ): Flow<ChatState> {
        var previousChatBoxState: ChatBox.State? = null

        return combine(
            participants.typingEvents(),
            chatBox.flatMapLatest { it.state },
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

    fun getChatInfo(
        participants: Flow<ChatParticipants>,
        usersDescription: Flow<UsersDescription>
    ): Flow<ChatInfo> {
        return combine(
            participants.otherParticipant(),
            usersDescription
        ) { participant, usersDesc ->
            ChatInfo(
                name = usersDesc.name(listOf(participant.userId)),
                image = usersDesc.image(listOf(participant.userId))
            )
        }
    }

    fun Flow<MessagesUI>.mapToConversationItems(
        firstUnreadMessageId: String?,
        shouldShowUnreadHeader: Flow<Boolean>
    ): Flow<List<ConversationItem>> {
        return combine(map { it.list }, shouldShowUnreadHeader) { messages, unreadHeader ->
            val items = mutableListOf<ConversationItem>()
            messages.forEachIndexed { index, message ->
                val previousMessage = messages.getOrNull(index + 1)

                items.add(ConversationItem.MessageItem(message.toUiMessage()))

                if (unreadHeader && message.id == firstUnreadMessageId) {
                    items.add(ConversationItem.UnreadMessagesItem)
                }

                if (previousMessage == null || !Iso8601.isSameDay(message.creationDate.time, previousMessage.creationDate.time)) {
                    items.add(ConversationItem.DayItem(message.creationDate.time))
                }
            }
            items
        }
    }

    suspend fun findFirstUnreadMessageId(
        messages: Messages,
        fetch: (Int, ((Result<Messages>) -> Unit)) -> Unit
    ): String? {
        val flow = MutableSharedFlow<String?>(replay = 1, extraBufferCapacity = 1)
        findFirstUnreadMessageInternal(messages, fetch) {
            flow.tryEmit(it)
        }
        return flow.first()
    }

    private fun findFirstUnreadMessageInternal(
        messages: Messages,
        fetch: (Int, ((Result<Messages>) -> Unit)) -> Unit,
        continuation: (String?) -> Unit
    ) {
        val list = messages.other
        val message = list.lastOrNull { it.state.value is Message.State.Received } ?: kotlin.run {
            continuation(null)
            return
        }
        val index = list.indexOf(message)
        val size = list.size

        if (index == size - 1) {
            fetch(5) {
                val result = it.getOrNull() ?: kotlin.run {
                    continuation(null)
                    return@fetch
                }
                if (result.other.isEmpty()) continuation(message.id)
                else findFirstUnreadMessageInternal(result, fetch, continuation)
            }
        }
        else continuation(message.id)
    }

    private fun Flow<ChatParticipants>.otherParticipant(): Flow<ChatParticipant> = map { it.others.first() }

    private fun Flow<ChatParticipants>.typingEvents(): Flow<ChatParticipant.Event> =
        otherParticipant().flatMapLatest { it.events.filterIsInstance<ChatParticipant.Event.Typing>() }

    private fun Flow<ChatParticipants>.otherParticipantState(): Flow<ChatParticipant.State> =
        otherParticipant().flatMapLatest { it.state }
}

