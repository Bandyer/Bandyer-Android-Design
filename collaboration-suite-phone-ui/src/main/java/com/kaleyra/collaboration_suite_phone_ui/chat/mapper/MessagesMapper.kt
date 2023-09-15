package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conversation.Messages
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform
import java.util.Date

object MessagesMapper {

    fun Message.toUiMessage(): Flow<com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message> {
        val text = (content as? Message.Content.Text)?.message ?: ""
        val time = TimestampUtils.parseTime(creationDate.time)

        return if (this is com.kaleyra.collaboration_suite.conversation.OtherMessage) {
            combine(creator.combinedDisplayName, creator.combinedDisplayImage.map { ImmutableUri(it ?: Uri.EMPTY) }) { name, image ->
                com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.OtherMessage(id, text, time, name ?: "",  image)
            }
        } else {
            flowOf(com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage(id, text, time, state.map { state -> mapToUiState(state) }))
        }
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
        return flatMapLatest { messagesUI ->
            val map = mutableMapOf<String, Pair<com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message, Date>>()
            val messagesList = messagesUI.list

            messagesList
                .map { message -> message.toUiMessage().map { Pair(it, message.creationDate) } }
                .merge()
                .transform { pair ->
                    val message = pair.first
                    map[message.id] = pair
                    val values = map.values.toList()
                    if (values.size == messagesList.size) {
                        emit(values)
                    }
                }.combine(shouldShowUnreadHeader) { pairs, unreadHeader ->
                    val items = mutableListOf<ConversationElement>()
                    var previousCreationDate: Date? = null
                    pairs.forEach { (message, creationDate) ->
                        items.add(ConversationElement.Message(message))

                        if (unreadHeader && message.id == firstUnreadMessageId) {
                            items.add(ConversationElement.UnreadMessages)
                        }

                        if (previousCreationDate == null || !TimestampUtils.isSameDay(creationDate.time, previousCreationDate!!.time)) {
                            items.add(ConversationElement.Day(creationDate.time))
                        }
                        previousCreationDate = creationDate
                    }
                    items
                }
        }.distinctUntilChanged()
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