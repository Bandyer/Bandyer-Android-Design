/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kaleyra.collaboration_suite_phone_ui.mapper.chat

import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conversation.Messages
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.Mocks.myMessageMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.now
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantEvents
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantState
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherTodayReadMessage
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherYesterdayUnreadMessage
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherTodayUnreadMessage
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherTodayUnreadMessage2
import com.kaleyra.collaboration_suite_phone_ui.Mocks.yesterday
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.mapToUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.toUiMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import java.util.Date

class MessagesMapperTest {

    @After
    fun tearDown() {
        otherParticipantState.value = ChatParticipant.State.Invited
        otherParticipantEvents.value = ChatParticipant.Event.Typing.Idle
    }

    @Test
    fun otherMessage_toUiState_mappedOtherMessage() = runTest {
        val message = spyk(otherTodayReadMessage)
        every { message.creator } returns mockk {
            every { userId } returns "creatorId"
        }
        val expected =
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.OtherMessage(
                userId = message.creator.userId,
                id = message.id,
                content = (message.content as? Message.Content.Text)?.message ?: "",
                time = TimestampUtils.parseTime(message.creationDate.time)
            )
        val result = message.toUiMessage()
        assertEquals(expected, result)
    }

    @Test
    fun myMessage_toUiState_mappedMyMessage() = runTest {
        val message = spyk(myMessageMock)
        every { message.creator } returns mockk {
            every { userId } returns "creatorId"
        }
        val expected =
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage(
                message.id,
                (message.content as? Message.Content.Text)?.message ?: "",
                TimestampUtils.parseTime(message.creationDate.time),
                message.state.mapToUiState()
            )
        val result = message.toUiMessage() as com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage
        assertEquals(expected.id, result.id)
        assertEquals(expected.content, result.content)
        assertEquals(expected.time, result.time)
        assertEquals(expected.state.first(), result.state.first())
    }

    @Test
    fun messageStateSending_mapToUiState_mappedStateSending() = runTest {
        val state = Message.State.Sending()
        val expected =
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sending
        val result = flowOf(state).mapToUiState().first()
        assertEquals(expected, result)
    }

    @Test
    fun messageStateSent_mapToUiState_mappedStateSent() = runTest {
        val state = Message.State.Sent()
        val expected =
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Sent
        val result = flowOf(state).mapToUiState().first()
        assertEquals(expected, result)
    }

    @Test
    fun messageStateReceived_mapToUiState_mappedStateReceived() = runTest {
        val state = Message.State.Received()
        val expected =
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Received
        val result = flowOf(state).mapToUiState().first()
        assertEquals(expected, result)
    }

    @Test
    fun messageStateCreated_mapToUiState_mappedStateCreated() = runTest {
        val state = Message.State.Created()
        val expected =
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Created
        val result = flowOf(state).mapToUiState().first()
        assertEquals(expected, result)
    }

    @Test
    fun messageStateRead_mapToUiState_mappedStateRead() = runTest {
        val state = Message.State.Read()
        val expected =
            com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.State.Read
        val result = flowOf(state).mapToUiState().first()
        assertEquals(expected, result)
    }

    @Test
    fun emptyMessagesList_mapToConversationItems_emptyMessageItems() = runTest {
        val result = listOf<Message>().mapToConversationItems()
        assert(result.isEmpty())
    }

    @Test
    fun `test is first and is last chain message flags when there are different users`() = runTest {
        val result = listOf(otherTodayReadMessage, otherTodayUnreadMessage, myMessageMock).mapToConversationItems()
        assert(isSameMessageItem(result[0], ConversationItem.Message(otherTodayReadMessage.toUiMessage(), isFirstChainMessage = false, isLastChainMessage = true)))
        assert(isSameMessageItem(result[1], ConversationItem.Message(otherTodayUnreadMessage.toUiMessage(), isFirstChainMessage = true, isLastChainMessage = false)))
        assert(isSameMessageItem(result[2], ConversationItem.Message(myMessageMock.toUiMessage(), isFirstChainMessage = true, isLastChainMessage = true)))
        assertEquals(ConversationItem.Day(myMessageMock.creationDate.time), result[3])
    }

    @Test
    fun `test is first and is last chain message flags with same user but different day`() = runTest {
        val result = listOf(otherTodayReadMessage, otherTodayUnreadMessage, otherYesterdayUnreadMessage).mapToConversationItems()
        assert(isSameMessageItem(result[0], ConversationItem.Message(otherTodayReadMessage.toUiMessage(), isFirstChainMessage = false, isLastChainMessage = true)))
        assert(isSameMessageItem(result[1], ConversationItem.Message(otherTodayUnreadMessage.toUiMessage(), isFirstChainMessage = true, isLastChainMessage = false)))
        assertEquals(ConversationItem.Day(otherTodayUnreadMessage.creationDate.time), result[2])
        assert(isSameMessageItem(result[3], ConversationItem.Message(otherYesterdayUnreadMessage.toUiMessage(), isFirstChainMessage = true, isLastChainMessage = true)))
        assertEquals(ConversationItem.Day(otherYesterdayUnreadMessage.creationDate.time), result[4])
    }

    @Test
    fun `test is first and is last chain message flags with three messages from the same user`() = runTest {
        val result = listOf(otherTodayUnreadMessage2, otherTodayUnreadMessage, otherTodayReadMessage).mapToConversationItems()
        assert(isSameMessageItem(result[0], ConversationItem.Message(otherTodayUnreadMessage2.toUiMessage(), isFirstChainMessage = false, isLastChainMessage = true)))
        assert(isSameMessageItem(result[1], ConversationItem.Message(otherTodayUnreadMessage.toUiMessage(), isFirstChainMessage = false, isLastChainMessage = false)))
        assert(isSameMessageItem(result[2], ConversationItem.Message(otherTodayReadMessage.toUiMessage(), isFirstChainMessage = true, isLastChainMessage = false)))
        assertEquals(ConversationItem.Day(otherTodayReadMessage.creationDate.time), result[3])
    }

    @Test
    fun twoMessageOnDifferentDays_mapToConversationItems_twoDayAndMessageItems() = runTest {
        val result = listOf(otherYesterdayUnreadMessage, myMessageMock).mapToConversationItems()
        assert(isSameMessageItem(result[0], ConversationItem.Message(otherYesterdayUnreadMessage.toUiMessage())))
        assertEquals(result[1], ConversationItem.Day(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[2], ConversationItem.Message(myMessageMock.toUiMessage())))
        assertEquals(result[3], ConversationItem.Day(now.toEpochMilli()))
    }

    @Test
    fun oneUnreadMessage_mapToConversationItems_unreadLabelShown() = runTest {
        val result = listOf(otherYesterdayUnreadMessage, myMessageMock).mapToConversationItems(firstUnreadMessageId = otherYesterdayUnreadMessage.id,)
        assert(isSameMessageItem(result[0], ConversationItem.Message(otherYesterdayUnreadMessage.toUiMessage())))
        assertEquals(result[1].javaClass, ConversationItem.UnreadMessages.javaClass)
        assertEquals(result[2], ConversationItem.Day(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[3], ConversationItem.Message(myMessageMock.toUiMessage())))
        assertEquals(result[4], ConversationItem.Day(now.toEpochMilli()))
    }

    @Test
    fun allMessagesRead_findFirstUnreadMessage_null() = runTest {
        val messages = mockk<Messages>()
        every { messages.other } returns listOf(
            otherTodayReadMessage,
            otherTodayReadMessage,
            otherTodayReadMessage
        )
        val fetch: (Int) -> Result<Messages> = { _: Int ->
            Result.success(messages)
        }
        val result = findFirstUnreadMessageId(messages, fetch)
        assertEquals(result, null)
    }

    @Test
    fun allMessagesUnread_findFirstUnreadMessage_lastUnreadMessageId() = runTest {
        val initMessages = mockk<Messages>()
        val messages = mockk<Messages>()
        val emptyMessages = mockk<Messages>()
        var fetched = false
        every { initMessages.other } returns listOf(otherYesterdayUnreadMessage)
        every { messages.other } returns listOf(
            otherYesterdayUnreadMessage,
            otherYesterdayUnreadMessage,
            otherTodayUnreadMessage
        )
        every { emptyMessages.other } returns listOf()
        val fetch: (Int) -> Result<Messages> = { _: Int ->
            if (!fetched) {
                fetched = true
                Result.success(messages)
            } else {
                Result.success(emptyMessages)
            }
        }
        val result = findFirstUnreadMessageId(initMessages, fetch)
        assertEquals(result, otherTodayUnreadMessage.id)
    }

    @Test
    fun mixedMessageState_findFirstUnreadMessage_lastUnreadMessageId() = runTest {
        val messages = mockk<Messages>()
        every { messages.other } returns listOf(
            otherYesterdayUnreadMessage,
            otherTodayUnreadMessage,
            otherTodayReadMessage
        )
        val fetch: (Int) -> Result<Messages> = { _: Int ->
            Result.success(messages)
        }
        val result = findFirstUnreadMessageId(messages, fetch)
        assertEquals(result, otherTodayUnreadMessage.id)
    }

    private suspend fun isSameMessageItem(
        item1: ConversationItem,
        item2: ConversationItem
    ): Boolean {
        if (item1 !is ConversationItem.Message) return false
        if (item2 !is ConversationItem.Message) return false

        if (item1 == item2) return true

        val message1 = item1.message
        val message2 = item2.message
        if (message1 !is com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage) return false
        if (message2 !is com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage) return false

        if (message1.id != message2.id) return false
        if (message1.content != message2.content) return false
        if (message1.time != message2.time) return false
        if (message1.state.first() != message2.state.first()) return false
        return true
    }
}