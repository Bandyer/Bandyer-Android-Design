package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conversation.Messages
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
        }.distinctUntilChanged()

    fun Message.toUiMessage(): com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message {
        val text = (content as? Message.Content.Text)?.message ?: ""
        val time = TimestampUtils.parseTime(creationDate.time)

        return if (this is com.kaleyra.collaboration_suite.conversation.OtherMessage)
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.OtherMessage(creator.userId, id, text, time)
        else
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage(creator.userId, id, text, time, state.mapToUiState())
    }

    private fun Flow<Message.State>.mapToUiState(): Flow<com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State> =
        map { state ->
            when (state) {
                is Message.State.Sending -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sending
                is Message.State.Sent -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sent
                else -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Read
            }
        }

    fun List<Message>.mapToConversationItems(unreadMessageId: String? = null, latestMessage: Message? = null): List<ConversationElement> {
        val items = mutableListOf<ConversationElement>()

        forEachIndexed { index, message ->
            val previousMessage = getOrNull(index + 1) ?: latestMessage
            val nextMessage = getOrNull(index - 1)
            val isMessageGroupClosed = nextMessage?.creator?.userId != message.creator.userId

            val messageElement = ConversationElement.Message(message = message.toUiMessage(), isMessageGroupClosed = isMessageGroupClosed)
            items.add(messageElement)

            if (unreadMessageId != null && message.id == unreadMessageId) {
                items.add(ConversationElement.UnreadMessages)
            }

            if (previousMessage == null || !TimestampUtils.isSameDay(message.creationDate.time, previousMessage.creationDate.time)) {
                items.add(ConversationElement.Day(message.creationDate.time))
            }

        }

        return items
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