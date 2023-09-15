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
import com.kaleyra.collaboration_suite.conversation.Messages
import com.kaleyra.collaboration_suite_phone_ui.Mocks.messagesUIMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.myMessageMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.now
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantEvents
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantState
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherReadMessageMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherUnreadMessageMock1
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherUnreadMessageMock2
import com.kaleyra.collaboration_suite_phone_ui.Mocks.yesterday
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.toUiMessage
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class MessagesMapperTest {

    @Before
    fun setUp() {
        every { messagesUIMock.my } returns listOf(myMessageMock)
        every { messagesUIMock.other } returns listOf(otherUnreadMessageMock1)
        every { messagesUIMock.list } returns messagesUIMock.other + messagesUIMock.my
    }

    @After
    fun tearDown() {
        otherParticipantState.value = ChatParticipant.State.Invited
        otherParticipantEvents.value = ChatParticipant.Event.Typing.Idle
    }

    @Test
    fun emptyMessagesList_mapToConversationItems_emptyMessageItems() = runTest {
        every { messagesUIMock.list } returns listOf()
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = "",
            shouldShowUnreadHeader = MutableStateFlow(false)
        ).first()
        assert(result.isEmpty())
    }

    @Test
    fun oneMessage_mapToConversationItems_dayAndMessageItems() = runTest {
        every { messagesUIMock.list } returns listOf(myMessageMock)
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = "",
            shouldShowUnreadHeader = MutableStateFlow(false)
        ).first()
        assert(isSameMessageItem(result[0], ConversationElement.Message(myMessageMock.toUiMessage())))
        assertEquals(result[1], ConversationElement.Day(now.toEpochMilli()))
    }

    @Test
    fun twoMessageOnDifferentDays_mapToConversationItems_twoDayAndMessageItems() = runTest {
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = "",
            shouldShowUnreadHeader = MutableStateFlow(false)
        ).first()
        assert(isSameMessageItem(result[0], ConversationElement.Message(otherUnreadMessageMock1.toUiMessage())))
        assertEquals(result[1], ConversationElement.Day(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[2], ConversationElement.Message(myMessageMock.toUiMessage())))
        assertEquals(result[3], ConversationElement.Day(now.toEpochMilli()))
    }

    @Test
    fun oneUnreadMessage_mapToConversationItems_unreadLabelShown() = runTest {
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = otherUnreadMessageMock1.id,
            shouldShowUnreadHeader = MutableStateFlow(true)
        ).first()
        assert(isSameMessageItem(result[0], ConversationElement.Message(otherUnreadMessageMock1.toUiMessage())))
        assertEquals(result[1].javaClass, ConversationElement.UnreadMessages.javaClass)
        assertEquals(result[2], ConversationElement.Day(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[3], ConversationElement.Message(myMessageMock.toUiMessage())))
        assertEquals(result[4], ConversationElement.Day(now.toEpochMilli()))
    }

    @Test
    fun allMessagesRead_findFirstUnreadMessage_null() = runTest {
        val messages = mockk<Messages>()
        every { messages.other } returns listOf(otherReadMessageMock, otherReadMessageMock, otherReadMessageMock)
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
        every { initMessages.other } returns listOf(otherUnreadMessageMock1)
        every { messages.other } returns listOf(otherUnreadMessageMock1, otherUnreadMessageMock1, otherUnreadMessageMock2)
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
        assertEquals(result, otherUnreadMessageMock2.id)
    }

    @Test
    fun mixedMessageState_findFirstUnreadMessage_lastUnreadMessageId() = runTest {
        val messages = mockk<Messages>()
        every { messages.other } returns listOf(otherUnreadMessageMock1, otherUnreadMessageMock2, otherReadMessageMock)
        val fetch: (Int) -> Result<Messages> = { _: Int ->
            Result.success(messages)
        }
        val result = findFirstUnreadMessageId(messages, fetch)
        assertEquals(result, otherUnreadMessageMock2.id)
    }

    private suspend fun isSameMessageItem(item1: ConversationElement, item2: ConversationElement): Boolean {
        if (item1 !is ConversationElement.Message) return false
        if (item2 !is ConversationElement.Message) return false

        if (item1 == item2) return true

        val message1 = item1.data
        val message2 = item2.data
        if (message1 !is com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage) return false
        if (message2 !is com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.MyMessage) return false

        if (message1.id != message2.id) return false
        if (message1.text != message2.text) return false
        if (message1.time != message2.time) return false
        if (message1.state.first() != message2.state.first()) return false
        return true
    }
}