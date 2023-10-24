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

import com.kaleyra.collaboration_suite.State
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conversation.*
import com.kaleyra.collaboration_suite_core_ui.*
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callState
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationState
import com.kaleyra.collaboration_suite_phone_ui.Mocks.messagesUIMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.myMessageMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantEvents
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantState
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherYesterdayUnreadMessage
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ChatActionsMapper.mapToChatActions
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatActionsMapperTest {

    @Before
    fun setUp() {
        every { callMock.state } returns callState
        every { conversationMock.state } returns conversationState
        every { messagesUIMock.my } returns listOf(myMessageMock)
        every { messagesUIMock.other } returns listOf(otherYesterdayUnreadMessage)
        every { messagesUIMock.list } returns messagesUIMock.other + messagesUIMock.my
    }

    @After
    fun tearDown() {
        unmockkAll()
        callState.value = Call.State.Connected
        conversationState.value = State.Connected
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

}