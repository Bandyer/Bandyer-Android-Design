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
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
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

    private var scrollReset = false

    private var isApproachingTop = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            MaterialTheme {
                Messages(
                    uiState = uiState.collectAsState().value,
                    onMessageScrolled = { },
                    onApproachingTop = { isApproachingTop = true },
                    onResetScroll = { scrollReset = true },
                    scrollState = LazyListState()
                )
            }
        }
    }

    @Test
    fun emptyMessages_noMessagesShown() {
        uiState.update { it.copy(conversationItems = emptyList()) }
        val noMessages = composeTestRule.activity.getString(R.string.kaleyra_chat_no_messages)
        composeTestRule.onNodeWithText(noMessages).assertIsDisplayed()
    }

    @Test
    fun messagesNotInitialized_loadingMessagingShown() {
        uiState.update { it.copy(areMessagesInitialized = false) }
        val channelLoading = composeTestRule.activity.getString(R.string.kaleyra_chat_channel_loading)
        composeTestRule.onNodeWithText(channelLoading).assertIsDisplayed()
    }

    @Test
    fun userScrollsUp_fabAppears() {
        composeTestRule.resetScrollFab.assertDoesNotExist()
        composeTestRule.onNodeWithTag(ConversationTag).performScrollUp()
        composeTestRule.resetScrollFab.assertIsDisplayed()
    }

    @Test
    fun userClicksFab_snapsToBottomAfterUserInteracted() {
        composeTestRule.onNodeWithTag(ConversationTag).performScrollUp()
        composeTestRule.resetScrollFab.performClick()
        composeTestRule.resetScrollFab.assertDoesNotExist()
        assert(scrollReset)
    }

    @Test
    fun userScrollsUp_onApproachingTopInvoked() {
        composeTestRule.onNodeWithTag(ConversationTag).performScrollUp()
        composeTestRule.onNodeWithTag(ConversationTag).performScrollUp()
        assert(isApproachingTop)
    }

    @Test
    fun messageStateSending_pendingIconShowed() {
        uiState.update { it.copy(conversationItems = listOf(ConversationItem.MessageItem(message))) }
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_pending)
        composeTestRule.onNodeWithTag(MessageStateTag).assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateSent_sentIconShowed() {
        val message = message.copy(state = MutableStateFlow(Message.State.Sent))
        uiState.update { it.copy(conversationItems =listOf(ConversationItem.MessageItem(message))) }
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_sent)
        composeTestRule.onNodeWithTag(MessageStateTag).assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateRead_seenIconShowed() {
        val message = message.copy(state = MutableStateFlow(Message.State.Read))
        uiState.update { it.copy(conversationItems = listOf(ConversationItem.MessageItem(message))) }
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_seen)
        composeTestRule.onNodeWithTag(MessageStateTag).assert(hasContentDescription(pendingStatus))
    }

    private val ComposeContentTestRule.resetScrollFab: SemanticsNodeInteraction
        get() = onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_scroll_to_last_message))

    private fun SemanticsNodeInteraction.performScrollUp() {
        performTouchInput {
            this.swipe(
                start = this.center,
                end = Offset(this.center.x, this.center.y + 500),
                durationMillis = 200
            )
        }
    }

}