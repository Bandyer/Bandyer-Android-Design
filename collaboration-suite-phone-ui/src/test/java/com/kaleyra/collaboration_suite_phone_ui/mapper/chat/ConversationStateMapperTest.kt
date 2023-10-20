package com.kaleyra.collaboration_suite_phone_ui.mapper.chat

import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.Conversation
import com.kaleyra.collaboration_suite_phone_ui.Mocks
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conferenceMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ConversationStateMapper.toConnectionState
import com.kaleyra.video_networking.connector.Connector
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
        Mocks.conversationState.value = Connector.State.Connected
        Mocks.otherParticipantState.value = ChatParticipant.State.Invited
        Mocks.otherParticipantEvents.value = ChatParticipant.Event.Typing.Idle
    }

    @Test
    fun conversationConnected_toConnectionState_connected() = runTest {
        Mocks.conversationState.value = Connector.State.Connected
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Connected)
    }

    @Test
    fun conversationConnecting_toConnectionState_connecting() = runTest {
        Mocks.conversationState.value = Connector.State.Connecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Connecting)
    }

    @Test
    fun conversationDisconnecting_toConnectionState_unknown() = runTest {
        Mocks.conversationState.value = Connector.State.Disconnecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationDisconnected_toConnectionState_unknown() = runTest {
        Mocks.conversationState.value = Connector.State.Disconnected
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationStateUserInactive_toConnectionState_error() = runTest {
        Mocks.conversationState.value = Connector.State.Disconnected.Error.UserInactive
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Error)
    }

    @Test
    fun conversationStateUnsupportedVersion_toConnectionState_error() = runTest {
        Mocks.conversationState.value = Connector.State.Disconnected.Error.UnsupportedVersion
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Error)
    }

    @Test
    fun conversationStateUnknown_toConnectionState_error() = runTest {
        Mocks.conversationState.value = Connector.State.Disconnected.Error.Unknown("")
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Error)
    }

    @Test
    fun conversationReconnecting_toConnectionState_offline() = runTest {
        with(flowOf(Mocks.conversationMock).toConnectionState()) {
            first()
            conversationState.value = Connector.State.Connecting
            assertEquals(first(), ConnectionState.Offline)
        }
    }

}