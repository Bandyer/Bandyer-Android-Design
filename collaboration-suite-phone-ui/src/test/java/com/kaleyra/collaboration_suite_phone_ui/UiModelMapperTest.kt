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
package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.chatbox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.model.Message.Companion.toUiMessage
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.getChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.getChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.mapToChatActions
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.mapToConversationItems
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class UiModelMapperTest {

    @Before
    fun setUp() {
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.state } returns callState
        every { chatBoxMock.state } returns chatBoxState
        every { messagesUIMock.my } returns listOf(myMessageMock)
        every { messagesUIMock.other } returns listOf(otherUnreadMessageMock1)
        every { messagesUIMock.list } returns messagesUIMock.other + messagesUIMock.my
    }

    @After
    fun tearDown() {
        unmockkAll()
        callState.value = Call.State.Connected
        chatBoxState.value = ChatBox.State.Connected
        otherParticipantState.value = ChatParticipant.State.Invited
        otherParticipantEvents.value = ChatParticipant.Event.Typing.Idle
    }

    @Test
    fun emptyActions_mapToUiActions_emptyUiActions() {
        assertEquals(setOf<ChatUI.Action>().mapToChatActions {}, setOf<ChatAction>())
    }

    @Test
    fun allActions_mapToUiActions_allUiActions() {
        val actions = ChatUI.Action.all
        val result = actions.mapToChatActions { }
        assert(result.filterIsInstance<ChatAction.AudioCall>().isNotEmpty())
        assert(result.filterIsInstance<ChatAction.AudioUpgradableCall>().isNotEmpty())
        assert(result.filterIsInstance<ChatAction.VideoCall>().isNotEmpty())
    }

    @Test
    fun callEnded_hasActiveCall_false() = runTest {
        callState.value = Call.State.Disconnected.Ended
        assert(!flowOf(phoneBoxMock).hasActiveCall().first())
    }

    @Test
    fun callDisconnected_hasActiveCall_true() = runTest {
        callState.value = Call.State.Disconnected
        assert(flowOf(phoneBoxMock).hasActiveCall().first())
    }

    @Test
    fun callConnecting_hasActiveCall_true() = runTest {
        callState.value = Call.State.Connecting
        assert(flowOf(phoneBoxMock).hasActiveCall().first())
    }

    @Test
    fun callConnected_hasActiveCall_true() = runTest {
        callState.value = Call.State.Connected
        assert(flowOf(phoneBoxMock).hasActiveCall().first())
    }

    @Test
    fun callReconnecting_hasActiveCall_true() = runTest {
        callState.value = Call.State.Reconnecting
        assert(flowOf(phoneBoxMock).hasActiveCall().first())
    }

    @Test
    fun chatBoxConnecting_getChatState_networkConnecting() = runTest {
        chatBoxState.value = ChatBox.State.Connecting
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.NetworkState.Connecting)
    }

    @Test
    fun chatBoxDisconnecting_getChatState_none() = runTest {
        chatBoxState.value = ChatBox.State.Disconnecting
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.None)
    }

    @Test
    fun chatBoxDisconnected_getChatState_none() = runTest {
        chatBoxState.value = ChatBox.State.Disconnected
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.None)
    }

    @Test
    fun chatBoxInitialized_getChatState_none() = runTest {
        chatBoxState.value = ChatBox.State.Initialized
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.None)
    }

    @Test
    fun chatBoxReconnecting_getChatState_networkOffline() = runTest {
        with(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock))) {
            first()
            chatBoxState.value = ChatBox.State.Connecting
            assertEquals(first(), ChatState.NetworkState.Offline)
        }
    }

    @Test
    fun participantOnline_getChatState_userOnline() = runTest {
        otherParticipantState.value = ChatParticipant.State.Joined.Online
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.UserState.Online)
    }

    @Test
    fun participantOfflineAt_getChatState_userOfflineTimestamp() = runTest {
        val nowMillis = now.toEpochMilli()
        otherParticipantState.value = ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.At(Date(nowMillis)))
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.UserState.Offline(nowMillis))
    }

    @Test
    fun participantOffline_getChatState_userOffline() = runTest {
        otherParticipantState.value = ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.Never)
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.UserState.Offline(null))
    }

    @Test
    fun participantTyping_getChatState_userTyping() = runTest {
        otherParticipantEvents.value = ChatParticipant.Event.Typing.Started
        assertEquals(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first(), ChatState.UserState.Typing)
    }

    // Does it makes sense?
    @Test
    fun usersDescription_getChatInfo_userIdAndImageUri() = runTest {
        val uriMock = mockk<Uri>()
        coEvery { usersDescriptionMock.name(any()) } returns otherParticipantMock.userId
        coEvery { usersDescriptionMock.image(any()) } returns uriMock
        assertEquals(getChatInfo(flowOf(chatParticipantsMock), flowOf(usersDescriptionMock)).first(), ChatInfo(otherParticipantMock.userId, ImmutableUri(uriMock)))
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
        assert(isSameMessageItem(result[0], ConversationItem.MessageItem(myMessageMock.toUiMessage())))
        assertEquals(result[1], ConversationItem.DayItem(now.toEpochMilli()))
    }

    @Test
    fun twoMessageOnDifferentDays_mapToConversationItems_twoDayAndMessageItems() = runTest {
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = "",
            shouldShowUnreadHeader = MutableStateFlow(false)
        ).first()
        assert(isSameMessageItem(result[0], ConversationItem.MessageItem(otherUnreadMessageMock1.toUiMessage())))
        assertEquals(result[1], ConversationItem.DayItem(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[2], ConversationItem.MessageItem(myMessageMock.toUiMessage())))
        assertEquals(result[3], ConversationItem.DayItem(now.toEpochMilli()))
    }

    @Test
    fun oneUnreadMessage_mapToConversationItems_unreadLabelShown() = runTest {
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = otherUnreadMessageMock1.id,
            shouldShowUnreadHeader = MutableStateFlow(true)
        ).first()
        assert(isSameMessageItem(result[0], ConversationItem.MessageItem(otherUnreadMessageMock1.toUiMessage())))
        assertEquals(result[1].javaClass, ConversationItem.UnreadMessagesItem.javaClass)
        assertEquals(result[2], ConversationItem.DayItem(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[3], ConversationItem.MessageItem(myMessageMock.toUiMessage())))
        assertEquals(result[4], ConversationItem.DayItem(now.toEpochMilli()))
    }

    @Test
    fun allMessagesRead_findFirstUnreadMessage_null() = runTest {
        val messages = mockk<Messages>()
        every { messages.other } returns listOf(otherReadMessageMock, otherReadMessageMock, otherReadMessageMock)
        val fetch = { _: Int, completion: (Result<Messages>) -> Unit ->
            completion(Result.success(messages))
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
        val fetch = { _: Int, completion: (Result<Messages>) -> Unit ->
            if (!fetched) {
                fetched = true
                completion(Result.success(messages))
            } else {
                completion(Result.success(emptyMessages))
            }
        }
        val result = findFirstUnreadMessageId(initMessages, fetch)
        assertEquals(result, otherUnreadMessageMock2.id)
    }

    @Test
    fun mixedMessageState_findFirstUnreadMessage_lastUnreadMessageId() = runTest {
        val messages = mockk<Messages>()
        every { messages.other } returns listOf(otherUnreadMessageMock1, otherUnreadMessageMock2, otherReadMessageMock)
        val fetch = { _: Int, completion: (Result<Messages>) -> Unit ->
            completion(Result.success(messages))
        }
        val result = findFirstUnreadMessageId(messages, fetch)
        assertEquals(result, otherUnreadMessageMock2.id)
    }

    private suspend fun isSameMessageItem(item1: ConversationItem, item2: ConversationItem): Boolean {
        if (item1 !is ConversationItem.MessageItem) return false
        if (item2 !is ConversationItem.MessageItem) return false

        if (item1 == item2) return true

        val message1 = item1.message
        val message2 = item2.message
        if (message1 !is com.kaleyra.collaboration_suite_phone_ui.chat.model.Message.MyMessage) return false
        if (message2 !is com.kaleyra.collaboration_suite_phone_ui.chat.model.Message.MyMessage) return false

        if (message1.id != message2.id) return false
        if (message1.text != message2.text) return false
        if (message1.time != message2.time) return false
        if (message1.state.first() != message2.state.first()) return false
        return true
    }
}