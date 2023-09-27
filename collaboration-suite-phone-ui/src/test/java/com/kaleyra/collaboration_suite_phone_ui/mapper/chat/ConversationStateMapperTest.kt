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
    fun conversationConnected_toConnectionState_connected() = runTest {
        Mocks.conversationState.value = Conversation.State.Connected
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Connected)
    }

    @Test
    fun conversationConnecting_toConnectionState_connecting() = runTest {
        Mocks.conversationState.value = Conversation.State.Connecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Connecting)
    }

    @Test
    fun conversationDisconnecting_toConnectionState_unknown() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationDisconnected_toConnectionState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationStateUserInactive_toConnectionState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected.UserInactive
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationStateUnsupportedVersion_toConnectionState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected.UnsupportedVersion
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationStateUnknown_toConnectionState_none() = runTest {
        Mocks.conversationState.value = Conversation.State.Disconnected.Unknown
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationReconnecting_toConnectionState_offline() = runTest {
        with(flowOf(Mocks.conversationMock).toConnectionState()) {
            first()
            conversationState.value = Conversation.State.Connecting
            assertEquals(first(), ConnectionState.Offline)
        }
    }

}