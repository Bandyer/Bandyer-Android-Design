/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.mapper.chat

import com.kaleyra.video.State
import com.kaleyra.video.conversation.ChatParticipant
import com.kaleyra.video_sdk.Mocks
import com.kaleyra.video_sdk.Mocks.conferenceMock
import com.kaleyra.video_sdk.Mocks.conversationState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.mapper.ConversationStateMapper.toConnectionState
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
        Mocks.conversationState.value = State.Connected
        Mocks.otherParticipantState.value = ChatParticipant.State.Invited
        Mocks.otherParticipantEvents.value = ChatParticipant.Event.Typing.Idle
    }

    @Test
    fun conversationConnected_toConnectionState_connected() = runTest {
        Mocks.conversationState.value = State.Connected
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Connected)
    }

    @Test
    fun conversationConnecting_toConnectionState_connecting() = runTest {
        Mocks.conversationState.value = State.Connecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Connecting)
    }

    @Test
    fun conversationDisconnecting_toConnectionState_unknown() = runTest {
        Mocks.conversationState.value = State.Disconnecting
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationDisconnected_toConnectionState_unknown() = runTest {
        Mocks.conversationState.value = State.Disconnected
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Unknown)
    }

    @Test
    fun conversationStateUserInactive_toConnectionState_error() = runTest {
        Mocks.conversationState.value = State.Disconnected.Error.UserInactive
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Error)
    }

    @Test
    fun conversationStateUnsupportedVersion_toConnectionState_error() = runTest {
        Mocks.conversationState.value = State.Disconnected.Error.UnsupportedVersion
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Error)
    }

    @Test
    fun conversationStateUnknown_toConnectionState_error() = runTest {
        Mocks.conversationState.value = State.Disconnected.Error.Unknown("")
        Assert.assertEquals(flowOf(Mocks.conversationMock).toConnectionState().first(), ConnectionState.Error)
    }

    @Test
    fun conversationReconnecting_toConnectionState_offline() = runTest {
        with(flowOf(Mocks.conversationMock).toConnectionState()) {
            first()
            conversationState.value = State.Connecting
            assertEquals(first(), ConnectionState.Offline)
        }
    }

}