package com.kaleyra.collaboration_suite_phone_ui.chat.utility

import android.net.Uri
import com.kaleyra.collaboration_suite.chatbox.*
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.ConferenceUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.model.Message.Companion.toUiMessage
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

    fun Flow<ConferenceUI>.hasActiveCall(): Flow<Boolean> =
        flatMapLatest { it.call }.flatMapLatest { it.state }
            .map { it !is Call.State.Disconnected.Ended }

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
                chatBoxState is ChatBox.State.Connected && participantState is ChatParticipant.State.Joined.Online && event is ChatParticipant.Event.Typing.Idle -> ChatState.UserState.Online
                chatBoxState is ChatBox.State.Connected && participantState is ChatParticipant.State.Joined.Offline && event is ChatParticipant.Event.Typing.Idle -> {
                    val lastLogin = participantState.lastLogin
                    ChatState.UserState.Offline(
                        if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At) lastLogin.date.time
                        else null
                    )
                }
                chatBoxState is ChatBox.State.Connected && event is ChatParticipant.Event.Typing.Started -> ChatState.UserState.Typing
                else -> ChatState.None
            }.also {
                previousChatBoxState = chatBoxState
            }
        }
    }

    fun getChatInfo(
        participants: Flow<ChatParticipants>
    ): Flow<ChatInfo> {
        val participant = participants.otherParticipant()
        return combine(participant.flatMapLatest { it.combinedDisplayName }, participant.flatMapLatest { it.combinedDisplayImage }) { name, image ->
            ChatInfo(name = name ?: "", image = ImmutableUri(image ?: Uri.EMPTY))
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

                if (previousMessage == null || !TimestampUtils.isSameDay(
                        message.creationDate.time,
                        previousMessage.creationDate.time
                    )
                ) {
                    items.add(ConversationItem.DayItem(message.creationDate.time))
                }
            }
            items
        }
    }

    suspend fun findFirstUnreadMessageId(
        messages: Messages,
        fetch: suspend (Int) -> Result<Messages>
    ): String? {
        val flow = MutableSharedFlow<String?>(replay = 1, extraBufferCapacity = 1)
        findFirstUnreadMessageInternal(messages, fetch) {
            flow.tryEmit(it)
        }
        return flow.first()
    }

    private suspend fun findFirstUnreadMessageInternal(
        messages: Messages,
        fetch: suspend (Int) -> Result<Messages>,
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
            val result = fetch(5).getOrNull()
            when {
                result == null -> continuation(null)
                result.other.isEmpty() -> continuation(message.id)
                else -> findFirstUnreadMessageInternal(result, fetch, continuation)
            }
        } else continuation(message.id)
    }

    private fun Flow<ChatParticipants>.otherParticipant(): Flow<ChatParticipant> =
        map { it.others.first() }

    private fun Flow<ChatParticipants>.typingEvents(): Flow<ChatParticipant.Event> =
        otherParticipant().flatMapLatest { it.events.filterIsInstance<ChatParticipant.Event.Typing>() }

    private fun Flow<ChatParticipants>.otherParticipantState(): Flow<ChatParticipant.State> =
        otherParticipant().flatMapLatest { it.state }
}

