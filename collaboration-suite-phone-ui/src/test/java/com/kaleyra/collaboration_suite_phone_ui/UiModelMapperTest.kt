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
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.ChatBoxUI
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.getChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.getChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.mapToUiActions
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
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
        every { messagesUIMock.my } returns listOf(myMessage)
        every { messagesUIMock.other } returns listOf(otherMessage)
        every { messagesUIMock.list } returns messagesUIMock.other + messagesUIMock.my
        every { chatParticipantsMock.others } returns listOf(otherParticipantMock)
        every { otherParticipantMock.userId } returns "userId"
        every { otherParticipantMock.state } returns otherParticipantState
        every { otherParticipantMock.events } returns otherParticipantEvents
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun emptyActions_mapToUiActions_emptyUiActions() {
        assert(setOf<ChatUI.Action>().mapToUiActions() == setOf<ChatAction>())
    }

    @Test
    fun allActions_mapToUiActions_allUiActions() {
        val actions = ChatUI.Action.all
        val result = setOf(ChatAction.AudioCall, ChatAction.AudioUpgradableCall, ChatAction.VideoCall)
        assert(actions.mapToUiActions() == result)
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
        assert(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first() == ChatState.NetworkState.Connecting)
    }

    @Test
    fun chatBoxDisconnected_getChatState_networkOffline() = runTest {
        with(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock))) {
            first()
            chatBoxState.value = ChatBox.State.Connecting
            assert(first() == ChatState.NetworkState.Offline)
        }
    }

    @Test
    fun participantOnline_getChatState_userOnline() = runTest {
        otherParticipantState.value = ChatParticipant.State.Joined.Online
        assert(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first() == ChatState.UserState.Online)
    }

    @Test
    fun participantOfflineAt_getChatState_userOfflineTimestamp() = runTest {
        val timestamp = "today"
        mockkObject(Iso8601)
        mockkObject(ContextRetainer)
        every { Iso8601.parseTimestamp(any(), any()) } returns timestamp
        every { ContextRetainer.context } returns mockk()
        otherParticipantState.value = ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.At(Date()))
        assert(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first() == ChatState.UserState.Offline(timestamp))
    }

    @Test
    fun participantOffline_getChatState_userOffline() = runTest {
        otherParticipantState.value = ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.Never)
        assert(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first() == ChatState.UserState.Offline(null))
    }

    @Test
    fun participantTyping_getChatState_userTyping() = runTest {
        otherParticipantEvents.value = ChatParticipant.Event.Typing.Started
        assert(getChatState(flowOf(chatParticipantsMock), flowOf(chatBoxMock)).first() == ChatState.UserState.Typing)
    }

    // Ha senso?
    @Test
    fun _getChatInfo_() = runTest {
        val name = "name"
        val image = mockk<Uri>()
        coEvery { usersDescriptionMock.name(listOf(otherParticipantMock.userId)) } returns name
        coEvery { usersDescriptionMock.image(listOf(otherParticipantMock.userId)) } returns image
        assert(getChatInfo(flowOf(chatParticipantsMock), flowOf(usersDescriptionMock)).first() == ChatInfo(name, image))
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
        every { messagesUIMock.list } returns listOf(myMessage)
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = "",
            shouldShowUnreadHeader = MutableStateFlow(false)
        ).first()
        assert(isSameMessageItem(result[0], ConversationItem.MessageItem(myMessage.toUiMessage())))
        assert(result[1] == ConversationItem.DayItem(now.toEpochMilli()))
    }

    @Test
    fun twoMessageOnDifferentDays_mapToConversationItems_twoDayAndMessageItems() = runTest {
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = "",
            shouldShowUnreadHeader = MutableStateFlow(false)
        ).first()
        assert(isSameMessageItem(result[0], ConversationItem.MessageItem(otherMessage.toUiMessage())))
        assert(result[1] == ConversationItem.DayItem(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[2], ConversationItem.MessageItem(myMessage.toUiMessage())))
        assert(result[3] == ConversationItem.DayItem(now.toEpochMilli()))
    }

    @Test
    fun oneUnreadMessage_mapToConversationItems_unreadLabelShown() = runTest {
        val result = flowOf(messagesUIMock).mapToConversationItems(
            firstUnreadMessageId = otherMessage.id,
            shouldShowUnreadHeader = MutableStateFlow(true)
        ).first()
        assert(isSameMessageItem(result[0], ConversationItem.MessageItem(otherMessage.toUiMessage())))
        assert(result[1] is ConversationItem.UnreadMessagesItem)
        assert(result[2] == ConversationItem.DayItem(yesterday.toEpochMilli()))
        assert(isSameMessageItem(result[3], ConversationItem.MessageItem(myMessage.toUiMessage())))
        assert(result[4] == ConversationItem.DayItem(now.toEpochMilli()))
    }
    private suspend fun isSameMessageItem(item1: ConversationItem, item2: ConversationItem): Boolean {
        if (item1 !is ConversationItem.MessageItem) return false
        if (item2 !is ConversationItem.MessageItem) return false

        if (item1 == item2) return true

        val message1 = item1.message
        val message2 = item2.message
        if (message1 !is com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.Message.MyMessage) return false
        if (message2 !is com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.Message.MyMessage) return false

        if (message1.id != message2.id) return false
        if (message1.text != message2.text) return false
        if (message1.time != message2.time) return false
        if (message1.state.first() != message2.state.first()) return false
        return true
    }
}