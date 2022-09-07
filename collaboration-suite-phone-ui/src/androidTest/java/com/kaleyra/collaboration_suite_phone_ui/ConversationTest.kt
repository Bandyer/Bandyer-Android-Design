/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.conversation.*
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.Message
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.mockConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ConversationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val message = Message.MyMessage("idTest", "Mutable state item", "18:00", MutableStateFlow(Message.State.Sending))

    private val uiState = MutableStateFlow(
        ConversationUiState(
            areMessagesInitialized = true,
            conversationItems = mockConversationItems.plus(mockConversationItems)
        )
    )

    private var onMessageScrolled = false

    private var onResetScroll = false

    private var onApproachingTop = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Messages(
                uiState = uiState.collectAsState().value,
                onMessageScrolled = { onMessageScrolled = true },
                onApproachingTop = { onApproachingTop = true },
                onResetScroll = { onResetScroll = true },
                scrollState = LazyListState()
            )
        }
    }

    @Test
    fun emptyMessages_noMessagesDisplayed() {
        uiState.update { it.copy(conversationItems = emptyList()) }
        val noMessages = composeTestRule.activity.getString(R.string.kaleyra_chat_no_messages)
        composeTestRule.onNodeWithText(noMessages).assertIsDisplayed()
    }

    @Test
    fun messagesNotInitialized_loadingMessagingDisplayed() {
        uiState.update { it.copy(areMessagesInitialized = false) }
        val channelLoading =
            composeTestRule.activity.getString(R.string.kaleyra_chat_channel_loading)
        composeTestRule.onNodeWithText(channelLoading).assertIsDisplayed()
    }

    @Test
    fun userScrollsUp_fabAppears() {
        findResetScrollFab().assertDoesNotExist()
        findConversation().performScrollUp()
        findResetScrollFab().assertIsDisplayed()
    }

    @Test
    fun userClicksFab_snapsToBottomAfterUserInteracted() {
        findConversation().performScrollUp()
        findResetScrollFab().performClick()
        findResetScrollFab().assertDoesNotExist()
        assert(onResetScroll)
    }

    @Test
    fun userScrollsUp_onApproachingTopInvoked() {
        findConversation().performScrollUp()
        findConversation().performScrollUp()
        assert(onApproachingTop)
    }

    @Test
    fun userScrollsUp_onMessageScrolledInvoked() {
        findConversation().performScrollUp()
        assert(onMessageScrolled)
    }

    @Test
    fun messageStateSending_pendingIconDisplayed() {
        uiState.update { it.copy(conversationItems = listOf(ConversationItem.MessageItem(message))) }
        val pendingStatus =
            composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_pending)
        findMessage().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateSent_sentIconDisplayed() {
        val message = message.copy(state = MutableStateFlow(Message.State.Sent))
        uiState.update { it.copy(conversationItems = listOf(ConversationItem.MessageItem(message))) }
        val pendingStatus =
            composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_sent)
        findMessage().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateRead_seenIconDisplayed() {
        val message = message.copy(state = MutableStateFlow(Message.State.Read))
        uiState.update { it.copy(conversationItems = listOf(ConversationItem.MessageItem(message))) }
        val pendingStatus =
            composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_seen)
        findMessage().assert(hasContentDescription(pendingStatus))
    }

    private fun findResetScrollFab() = composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_scroll_to_last_message))

    private fun findConversation() = composeTestRule.onNodeWithTag(ConversationTag)

    private fun findMessage() = composeTestRule.onNodeWithTag(MessageStateTag)

}