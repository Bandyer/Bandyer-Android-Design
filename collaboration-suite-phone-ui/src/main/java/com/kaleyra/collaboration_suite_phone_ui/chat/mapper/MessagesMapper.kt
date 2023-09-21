package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conversation.Messages
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

typealias ParticipantDetails = Pair<String, ImmutableUri>

object MessagesMapper {

    fun Message.toUiMessage(): com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message {
        val text = (content as? Message.Content.Text)?.message ?: ""
        val time = TimestampUtils.parseTime(creationDate.time)

        return if (this is com.kaleyra.collaboration_suite.conversation.OtherMessage)
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.OtherMessage(id, text, time, creator.userId)
        else
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage(id, text, time, state.mapToUiState())
    }

    fun Flow<Message.State>.mapToUiState(): Flow<com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State> =
        map { state ->
            when (state) {
                is Message.State.Sending -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sending
                is Message.State.Sent -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sent
                is Message.State.Received -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Received
                is Message.State.Created -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Created
                else -> com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Read
            }
        }

    fun List<Message>.mapToConversationItems(firstUnreadMessageId: String? = null, lastMappedMessage: Message? = null): List<ConversationElement> {
        val items = mutableListOf<ConversationElement>()

        forEachIndexed { index, message ->
            val previousMessage = getOrNull(index + 1) ?: lastMappedMessage
            val nextMessage = getOrNull(index - 1)
            val isFirstChainMessage = previousMessage?.creator?.userId != message.creator.userId
            val isLastChainMessage = nextMessage?.creator?.userId != message.creator.userId

            val messageElement = ConversationElement.Message(
                message = message.toUiMessage(),
                isFirstChainMessage = isFirstChainMessage,
                isLastChainMessage = isLastChainMessage
            )
            items.add(messageElement)

            if (firstUnreadMessageId != null && message.id == firstUnreadMessageId) {
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