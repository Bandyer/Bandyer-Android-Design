package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conversation.Messages
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object MessagesMapper {

    fun Message.toUiMessage(): com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message {
        val text = (content as? Message.Content.Text)?.message ?: ""
        val time = TimestampUtils.parseTime(creationDate.time)

        return if (this is com.kaleyra.collaboration_suite.conversation.OtherMessage) com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.OtherMessage(id, text, time)
        else com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage(id, text, time, state.map { state -> mapToUiState(state) })
    }

    private fun mapToUiState(state: Message.State): com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State =
        when (state) {
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

                if (previousMessage == null || !TimestampUtils.isSameDay(
                        message.creationDate.time,
                        previousMessage.creationDate.time
                    )
                ) {
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