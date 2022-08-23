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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.conversation.ConversationTag
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.conversation.Messages
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.mockConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ConversationUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyMessages_noMessagesShown() {
        val noMessages = composeTestRule.activity.getString(R.string.kaleyra_chat_no_messages)
        composeTestRule.launchMessagesUI(ConversationUiState(areMessagesInitialized = true))
        composeTestRule.onNodeWithText(noMessages).assertIsDisplayed()
    }

    @Test
    fun messagesNotInitialized_loadingMessagingShown() {
        val channelLoading = composeTestRule.activity.getString(R.string.kaleyra_chat_channel_loading)
        composeTestRule.launchMessagesUI(ConversationUiState(areMessagesInitialized = false))
        composeTestRule.onNodeWithText(channelLoading).assertIsDisplayed()
    }

    @Test
    fun userScrollsUp_fabAppears() {
        composeTestRule.launchMessagesUI(
            ConversationUiState(
                areMessagesInitialized = true,
                conversationItems = mockConversationItems.plus(mockConversationItems)
            )
        )
        composeTestRule.scrollToBottomFab.assertDoesNotExist()
        composeTestRule.conversation.performScrollUp()
        composeTestRule.scrollToBottomFab.assertIsDisplayed()
    }

    @Test
    fun userClicksFab_snapsToBottomAfterUserInteracted() {
        composeTestRule.launchMessagesUI(
            ConversationUiState(
                areMessagesInitialized = true,
                conversationItems = mockConversationItems.plus(mockConversationItems)
            )
        )
        composeTestRule.conversation.performScrollUp()
        composeTestRule.scrollToBottomFab.performClick()
        composeTestRule.scrollToBottomFab.assertDoesNotExist()
    }

    private fun ComposeContentTestRule.launchMessagesUI(conversationUiState: ConversationUiState) {
        setContent {
            MaterialTheme {
                Messages(
                    uiState = conversationUiState,
                    onMessageScrolled = { },
                    onFetchMessages = { },
                    onAllMessagesScrolled = { },
                    onReadAllMessages = { },
                    scrollState = LazyListState()
                )
            }
        }
    }

    private val ComposeContentTestRule.scrollToBottomFab: SemanticsNodeInteraction
        get() = onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_scroll_to_last_message))

    private val ComposeContentTestRule.conversation: SemanticsNodeInteraction
        get() = onNodeWithTag(ConversationTag)

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