package com.kaleyra.collaboration_suite_phone_ui.mapper.chat

import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.Conversation
import com.kaleyra.collaboration_suite_phone_ui.Mocks
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conferenceMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ConversationStateMapper.toConnectionState
import io.mockk.every
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

class ConversationStateMapperTest {

    @Before
    fun setUp() {
        every { conferenceMock.call } returns MutableStateFlow(Mocks.callMock)
        every { Mocks.callMock.state } returns Mocks.callState
        every { Mocks.conversationMock.state } returns Mocks.conversationState
    }

    @After
    fun tearDown() {
        Mocks.conversationState.value = Conversation.State.Connected
        Mocks.otherParticipantState.value = ChatParticipant.State.Invited
        Mocks.otherParticipantEvents.value = ChatParticipant.Event.Typing.Idle
    }
    
    @Test
    fun conversationConnecting_getChatState_networkConnecting() = runTest {
        Mocks.conversationState.value = Conversation.State.Connecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.NetworkState.Connecting)
    }

    @Test
    fun conversationDisconnecting_getChatState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.Undefined)
    }

    @Test
    fun conversationDisconnected_getChatState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.Undefined)
    }

    @Test
    fun conversationStateUserInactive_getChatState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected.UserInactive
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.Undefined)
    }

    @Test
    fun conversationStateUnsupportedVersion_getChatState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected.UnsupportedVersion
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.Undefined)
    }

    @Test
    fun conversationStateUnknown_getChatState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected.Unknown
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.Undefined)
    }

    @Test
    fun conversationReconnecting_getChatState_networkOffline() = runTest {
        with(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock))) {
            first()
            conversationState.value = Conversation.State.Connecting
            assertEquals(first(), ConnectionState.NetworkState.Offline)
        }
    }

    @Test
    fun participantOnline_getChatState_userOnline() = runTest {
        Mocks.otherParticipantState.value = ChatParticipant.State.Joined.Online
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.UserState.Online)
    }

    @Test
    fun participantOfflineAt_getChatState_userOfflineTimestamp() = runTest {
        val nowMillis = Mocks.now.toEpochMilli()
        Mocks.otherParticipantState.value = ChatParticipant.State.Joined.Offline(
            ChatParticipant.State.Joined.Offline.LastLogin.At(
                Date(nowMillis)
            ))
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.UserState.Offline(nowMillis))
    }

    @Test
    fun participantOffline_getChatState_userOffline() = runTest {
        Mocks.otherParticipantState.value = ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.Never)
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.UserState.Offline(null))
    }

    @Test
    fun participantTyping_getChatState_userTyping() = runTest {
        Mocks.otherParticipantEvents.value = ChatParticipant.Event.Typing.Started
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState(flowOf(Mocks.chatParticipantsMock)).first(), ConnectionState.UserState.Typing)
    }
}