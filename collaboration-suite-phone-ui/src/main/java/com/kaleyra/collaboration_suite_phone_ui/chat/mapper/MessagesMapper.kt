package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conversation.Messages
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

typealias ParticipantDetails = Pair<String, ImmutableUri>

object MessagesMapper {

    fun Flow<ChatParticipants>.toChatParticipantUserDetails(): Flow<ImmutableMap<String, ParticipantDetails>> =
        flatMapLatest { chatParticipants ->
                val participantsList = chatParticipants.list
                val users = mutableMapOf<String, ParticipantDetails>()
                participantsList
                    .map { participant ->
                        combine(
                            participant.combinedDisplayName,
                            participant.combinedDisplayImage.map { ImmutableUri(it ?: Uri.EMPTY) }
                        ) { name, image -> Triple(participant.userId, name ?: "", image) }
                    }
                    .merge()
                    .transform { (userId, name, image) ->
                        users[userId] = ParticipantDetails(name, image)
                        val values = users.values.toList()
                        if (values.size == participantsList.size) {
                            emit(ImmutableMap(users))
                        }
                    }
            }
            .distinctUntilChanged()

    fun Flow<MessagesUI>.toMyMessagesSendStates(): Flow<ImmutableMap<String, com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State>> {
        return this.flatMapLatest { messagesUI ->
            val states = mutableMapOf<String, com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State>()
            val messages = messagesUI.my

            messages
                .map { message -> message.state.map { Pair(message.id, it) } }
                .merge()
                .transform { (messageId, messageState) ->
                    states[messageId] = messageState.mapToUiState()
                    if (messages.size == states.keys.size) {
                        emit(ImmutableMap(states))
                    }
                }
        }
    }

    fun Message.toUiMessage(): com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message {
        val text = (content as? Message.Content.Text)?.message ?: ""
        val time = TimestampUtils.parseTime(creationDate.time)

        return if (this is com.kaleyra.collaboration_suite.conversation.OtherMessage) {
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.OtherMessage(id, text, time)
        } else {
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage(id, text, time)
        }
    }

    private fun Message.State.mapToUiState(): com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State =
        when (this) {
            is Message.State.Sending -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sending
            is Message.State.Sent -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sent
            else -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Read
        }

    fun Flow<MessagesUI>.mapToConversationItems(
        firstUnreadMessageId: String?,
        shouldShowUnreadHeader: Flow<Boolean>
    ): Flow<List<ConversationElement>> {
        return combine(map { it.list }, shouldShowUnreadHeader) { messages, unreadHeader ->
            val items = mutableListOf<ConversationElement>()
            messages.forEachIndexed { index, message ->
                val previousMessage = messages.getOrNull(index + 1)

                items.add(ConversationElement.Message(message.toUiMessage()))

                if (unreadHeader && message.id == firstUnreadMessageId) {
                    items.add(ConversationElement.UnreadMessages)
                }

                if (previousMessage == null || !TimestampUtils.isSameDay(message.creationDate.time, previousMessage.creationDate.time)) {
                    items.add(ConversationElement.Day(message.creationDate.time))
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
}