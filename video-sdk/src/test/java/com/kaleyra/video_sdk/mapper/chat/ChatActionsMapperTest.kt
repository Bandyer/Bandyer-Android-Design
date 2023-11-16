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
package com.kaleyra.video_sdk.mapper.chat

import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conversation.*
import com.kaleyra.video_common_ui.*
import com.kaleyra.video_sdk.Mocks.callMock
import com.kaleyra.video_sdk.Mocks.callState
import com.kaleyra.video_sdk.Mocks.conversationMock
import com.kaleyra.video_sdk.Mocks.conversationState
import com.kaleyra.video_sdk.Mocks.messagesUIMock
import com.kaleyra.video_sdk.Mocks.myMessageMock
import com.kaleyra.video_sdk.Mocks.otherParticipantEvents
import com.kaleyra.video_sdk.Mocks.otherParticipantState
import com.kaleyra.video_sdk.Mocks.otherYesterdayUnreadMessage
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.mapper.ChatActionsMapper.mapToChatActions
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
        assertEquals(setOf<ChatUI.Action>().mapToChatActions { _, _ ->}, setOf<ChatAction>())
    }

    @Test
    fun allActions_mapToUiActions_allUiActions() {
        val actions = ChatUI.Action.all
        val result = actions.mapToChatActions { _, _ -> }
        assert(result.filterIsInstance<ChatAction.AudioCall>().isNotEmpty())
        assert(result.filterIsInstance<ChatAction.AudioUpgradableCall>().isNotEmpty())
        assert(result.filterIsInstance<ChatAction.VideoCall>().isNotEmpty())
    }

}