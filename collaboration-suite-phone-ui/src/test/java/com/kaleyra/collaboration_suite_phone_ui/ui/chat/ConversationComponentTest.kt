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

package com.kaleyra.collaboration_suite_phone_ui.ui.chat

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.MessageStateTag
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.ConversationComponent
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.ConversationContentTag
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.ProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.ui.performScrollUp
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConversationComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val message = Message.MyMessage("idTest", "Mutable state item", "18:00", MutableStateFlow(
        Message.State.Sending))

    private var onMessageScrolled = false

    private var onResetScroll = false

    private var onApproachingTop = false

    @After
    fun tearDown() {
        onMessageScrolled = false
        onResetScroll = false
        onApproachingTop = false
    }

    @Test
    fun emptyMessages_noMessagesDisplayed() {
        setContent(ConversationUiState(conversationElements = ImmutableList(emptyList())))
        val noMessages = composeTestRule.activity.getString(R.string.kaleyra_chat_no_messages)
        composeTestRule.onNodeWithText(noMessages).assertIsDisplayed()
    }

    @Test
    fun messagesNotInitialized_loadingMessagingDisplayed() {
        setContent(ConversationUiState(conversationElements = null))
        val channelLoading = composeTestRule.activity.getString(R.string.kaleyra_chat_channel_loading)
        composeTestRule.onNodeWithText(channelLoading).assertIsDisplayed()
    }

    @Test
    fun userScrollsUp_fabAppears() {
        setContent(ConversationUiState(conversationElements = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findResetScrollFab().assertDoesNotExist()
        findConversation().performScrollUp()
        findResetScrollFab().assertIsDisplayed()
    }

    @Test
    fun userClicksFab_resetScrollInvoked() {
        setContent(ConversationUiState(conversationElements = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findConversation().performScrollUp()
        findResetScrollFab().performClick()
        findResetScrollFab().assertDoesNotExist()
        assert(onResetScroll)
    }

    @Test
    fun userScrollsToTop_onApproachingTopInvoked() {
        setContent(ConversationUiState(conversationElements = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findConversation().performScrollUp()
        findConversation().performScrollUp()
        assert(onApproachingTop)
    }

    @Test
    fun userScrollsUp_onMessageScrolledInvoked() {
        setContent(ConversationUiState(conversationElements = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findConversation().performScrollUp()
        assert(onMessageScrolled)
    }

    @Test
    fun messageStateSending_pendingIconDisplayed() {
        setContent(ConversationUiState(conversationElements = ImmutableList(listOf(ConversationElement.Message(message)))))
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_pending)
        findMessageState().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateSent_sentIconDisplayed() {
        val message = message.copy(state = MutableStateFlow(Message.State.Sent))
        setContent(ConversationUiState(conversationElements = ImmutableList(listOf(ConversationElement.Message(message)))))
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_sent)
        findMessageState().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateRead_seenIconDisplayed() {
        val message = message.copy(state = MutableStateFlow(Message.State.Read))
        setContent(ConversationUiState(conversationElements = ImmutableList(listOf(ConversationElement.Message(message)))))
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_seen)
        findMessageState().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun isNotFetching_progressIndicatorNotDisplayed() {
        setContent(ConversationUiState(isFetching = false, conversationElements = ImmutableList(listOf(ConversationElement.Message(message)))))
        findProgressIndicator().assertDoesNotExist()
    }

    @Test
    fun isFetching_progressIndicatorDisplayed() {
        setContent(ConversationUiState(isFetching = true, conversationElements = ImmutableList(listOf(ConversationElement.Message(message)))))
        findProgressIndicator().assertIsDisplayed()
    }

    private fun findResetScrollFab() = composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(
        R.string.kaleyra_chat_scroll_to_last_message
    ))

    private fun findConversation() = composeTestRule.onNodeWithTag(ConversationContentTag)

    private fun findMessageState() = composeTestRule.onNodeWithTag(MessageStateTag)

    private fun findProgressIndicator() = composeTestRule.onNodeWithTag(ProgressIndicatorTag)

    private fun setContent(uiState: ConversationUiState) = composeTestRule.setContent {
        ConversationComponent(
            uiState = uiState,
            onMessageScrolled = { onMessageScrolled = true },
            onApproachingTop = { onApproachingTop = true },
            onResetScroll = { onResetScroll = true },
            scrollState = LazyListState()
        )
    }

}