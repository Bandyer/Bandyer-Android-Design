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

package com.kaleyra.video_sdk.chat

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.conversation.ConversationComponent
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.model.ConversationState
import com.kaleyra.video_sdk.chat.conversation.model.Message
import com.kaleyra.video_sdk.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.video_sdk.chat.conversation.view.ConversationContentPadding
import com.kaleyra.video_sdk.chat.conversation.view.ConversationContentTag
import com.kaleyra.video_sdk.chat.conversation.view.MessageStateTag
import com.kaleyra.video_sdk.chat.conversation.view.ProgressIndicatorTag
import com.kaleyra.video_sdk.chat.conversation.view.item.OtherBubbleLeftSpacing
import com.kaleyra.video_sdk.chat.conversation.view.item.BubbleTestTag
import com.kaleyra.video_sdk.chat.conversation.view.item.OtherBubbleAvatarSpacing
import com.kaleyra.video_sdk.chat.conversation.view.item.MessageItemAvatarSize
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.findAvatar
import com.kaleyra.video_sdk.performScrollUp
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val myMessage = Message.MyMessage("idTest", "Mutable state item", "18:00", MutableStateFlow(Message.State.Sending))

    private val otherMessage = Message.OtherMessage(
        "id4",
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
        "13:12",
        "userId4"
    )

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
        setContent(ConversationState(conversationItems = ImmutableList(emptyList())))
        val noMessages = composeTestRule.activity.getString(R.string.kaleyra_chat_no_messages)
        composeTestRule.onNodeWithText(noMessages).assertIsDisplayed()
    }

    @Test
    fun messagesNotInitialized_loadingMessagingDisplayed() {
        setContent(ConversationState(conversationItems = null))
        val channelLoading = composeTestRule.activity.getString(R.string.kaleyra_chat_channel_loading)
        composeTestRule.onNodeWithText(channelLoading).assertIsDisplayed()
    }

    @Test
    fun userScrollsUp_fabAppears() {
        setContent(ConversationState(conversationItems = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findResetScrollFab().assertDoesNotExist()
        findConversation().performScrollUp()
        findResetScrollFab().assertIsDisplayed()
    }

    @Test
    fun userClicksFab_resetScrollInvoked() {
        setContent(ConversationState(conversationItems = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findConversation().performScrollUp()
        findResetScrollFab().performClick()
        findResetScrollFab().assertDoesNotExist()
        assert(onResetScroll)
    }

    @Test
    fun userScrollsToTop_onApproachingTopInvoked() {
        setContent(ConversationState(conversationItems = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findConversation().performScrollUp()
        findConversation().performScrollUp()
        assert(onApproachingTop)
    }

    @Test
    fun userScrollsUp_onMessageScrolledInvoked() {
        setContent(ConversationState(conversationItems = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))))
        findConversation().performScrollUp()
        assert(onMessageScrolled)
    }

    @Test
    fun messageStateSending_pendingIconDisplayed() {
        setContent(ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(myMessage)))))
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_pending)
        findMessageState().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateSent_sentIconDisplayed() {
        val message = myMessage.copy(state = MutableStateFlow(Message.State.Sent))
        setContent(ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(message)))))
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_sent)
        findMessageState().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun messageStateRead_seenIconDisplayed() {
        val message = myMessage.copy(state = MutableStateFlow(Message.State.Read))
        setContent(ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(message)))))
        val pendingStatus = composeTestRule.activity.getString(R.string.kaleyra_chat_msg_status_seen)
        findMessageState().assert(hasContentDescription(pendingStatus))
    }

    @Test
    fun isNotFetching_progressIndicatorNotDisplayed() {
        setContent(ConversationState(isFetching = false, conversationItems = ImmutableList(listOf(ConversationItem.Message(myMessage)))))
        findProgressIndicator().assertDoesNotExist()
    }

    @Test
    fun isFetching_progressIndicatorDisplayed() {
        setContent(ConversationState(isFetching = true, conversationItems = ImmutableList(listOf(ConversationItem.Message(myMessage)))))
        findProgressIndicator().assertIsDisplayed()
    }

    @Test
    fun participantDetailsProvided_latestGroupMessageOfOtherUser_avatarIsDisplayed() {
        setContent(
            ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(otherMessage, isLastChainMessage = true)))),
            participantsDetails = ImmutableMap(mapOf("userId4" to ChatParticipantDetails("username")))
        )
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun participantDetailsProvided_usernameIsDisplayed() {
        setContent(
            ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(otherMessage)))),
            participantsDetails = ImmutableMap(mapOf("userId4" to ChatParticipantDetails("otherUsername")))
        )
        composeTestRule.onNodeWithText("otherUsername").assertIsDisplayed()
    }

    @Test
    fun participantDetailsProvided_notLatestGroupMessageOfOtherUser_bubbleIsSpaced() {
        setContent(
            ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(otherMessage)))),
            participantsDetails = ImmutableMap(mapOf("userId4" to ChatParticipantDetails("otherUsername")))
        )
        composeTestRule.onNodeWithTag(BubbleTestTag).assertLeftPositionInRootIsEqualTo(ConversationContentPadding + OtherBubbleLeftSpacing)
    }

    @Test
    fun participantDetailsProvided_latestGroupMessageOfOtherUser_bubbleIsSpacedFromAvatar() {
        setContent(
            ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(otherMessage, isLastChainMessage = true)))),
            participantsDetails = ImmutableMap(mapOf("userId4" to ChatParticipantDetails("username")))
        )
        composeTestRule.onNodeWithTag(BubbleTestTag).assertLeftPositionInRootIsEqualTo(ConversationContentPadding + MessageItemAvatarSize + OtherBubbleAvatarSpacing)
    }

    @Test
    fun participantDetailsNotProvided_otherUserMessage_bubbleIsNotSpaced() {
        setContent(ConversationState(conversationItems = ImmutableList(listOf(ConversationItem.Message(otherMessage, isLastChainMessage = true)))))
        composeTestRule.onNodeWithTag(BubbleTestTag).assertLeftPositionInRootIsEqualTo(ConversationContentPadding)
    }

    private fun findResetScrollFab() = composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(
        R.string.kaleyra_chat_scroll_to_last_message
    ))

    private fun findConversation() = composeTestRule.onNodeWithTag(ConversationContentTag)

    private fun findMessageState() = composeTestRule.onNodeWithTag(MessageStateTag)

    private fun findProgressIndicator() = composeTestRule.onNodeWithTag(ProgressIndicatorTag)

    private fun setContent(uiState: ConversationState, participantsDetails: ImmutableMap<String, ChatParticipantDetails> = ImmutableMap()) = composeTestRule.setContent {
        ConversationComponent(
            conversationState = uiState,
            participantsDetails = participantsDetails,
            onMessageScrolled = { onMessageScrolled = true },
            onApproachingTop = { onApproachingTop = true },
            onResetScroll = { onResetScroll = true }
        )
    }

}