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

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callState
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.CallStateMapper.hasActiveCall
import io.mockk.every
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class CallStateMapperTest {

    @Before
    fun setUp() {
        every { callMock.state } returns callState
        every { conversationMock.state } returns conversationState
    }

    @After
    fun tearDown() {
        callState.value = Call.State.Connected
    }

    @Test
    fun callEnded_hasActiveCall_false() = runTest {
        callState.value = Call.State.Disconnected.Ended
        assert(!flowOf(callMock).hasActiveCall().first())
    }

    @Test
    fun callDisconnected_hasActiveCall_true() = runTest {
        callState.value = Call.State.Disconnected
        assert(flowOf(callMock).hasActiveCall().first())
    }

    @Test
    fun callConnecting_hasActiveCall_true() = runTest {
        callState.value = Call.State.Connecting
        assert(flowOf(callMock).hasActiveCall().first())
    }

    @Test
    fun callConnected_hasActiveCall_true() = runTest {
        callState.value = Call.State.Connected
        assert(flowOf(callMock).hasActiveCall().first())
    }

    @Test
    fun callReconnecting_hasActiveCall_true() = runTest {
        callState.value = Call.State.Reconnecting
        assert(flowOf(callMock).hasActiveCall().first())
    }
}