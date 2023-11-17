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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.*
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantsState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.appbar.view.BouncingDotsTag
import com.kaleyra.video_sdk.chat.appbar.view.ChatActionsTag
import com.kaleyra.video_sdk.chat.appbar.view.GroupAppBar
import com.kaleyra.video_sdk.chat.appbar.view.SubtitleTag
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.common.topappbar.ActionsTag
import com.kaleyra.video_sdk.findBackButton
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class GroupAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Unknown)

    private var isInCall by mutableStateOf(false)

    private var participantsDetails by mutableStateOf(ImmutableMap<String, ChatParticipantDetails>())

    private var participantsState by mutableStateOf(ChatParticipantsState())

    private val chatActions = ImmutableSet(setOf<ChatAction>(ChatAction.AudioCall { isActionClicked = true }))

    private var isBackPressed = false

    private var isActionClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            GroupAppBar(
                image = ImmutableUri(),
                name = "chatName",
                connectionState = connectionState,
                participantsDetails = participantsDetails,
                participantsState = participantsState,
                isInCall = isInCall ,
                actions = chatActions,
                onBackPressed = { isBackPressed = true }
            )
        }
    }

    @After
    fun tearDown() {
        connectionState = ConnectionState.Unknown
        participantsDetails = ImmutableMap()
        participantsState = ChatParticipantsState()
        isInCall = false
        isBackPressed = false
        isActionClicked = false
    }

    // Check the content description instead of the text because the title and subtitle views are AndroidViews
    @Test
    fun title_set() {
        composeTestRule.onNodeWithContentDescription("chatName").assertIsDisplayed()
    }

    @Test
    fun chatStateNetworkConnecting_connectingDisplayed() {
        connectionState = ConnectionState.Connecting
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_chat_state_connecting)
        getSubtitle().assertContentDescriptionEquals(connecting)
    }

    @Test
    fun chatStateNetworkOffline_waitingForNetworkDisplayed() {
        connectionState = ConnectionState.Offline
        val waitingForNetwork = composeTestRule.activity.getString(R.string.kaleyra_chat_state_waiting_for_network)
        getSubtitle().assertContentDescriptionEquals(waitingForNetwork)
    }

    @Test
    fun participantsStateOneTyping_typingIsDisplayed() {
        val users = listOf("mary")
        participantsState = ChatParticipantsState(typing = ImmutableList(users))
        val typing = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participants_typing, 1, users[0])
        getSubtitle().assertContentDescriptionEquals(typing)
    }

    @Test
    fun participantsStateManyAreTyping_typingIsDisplayed() {
        val users = listOf("mary", "john")
        participantsState = ChatParticipantsState(typing = ImmutableList(users))
        val typing = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participants_typing, users.size, users.size)
        getSubtitle().assertContentDescriptionEquals(typing)
    }

    @Test
    fun participantsStateOnline_onlineIsDisplayed() {
        val users = listOf("mary", "john")
        participantsState = ChatParticipantsState(online = ImmutableList(users))
        val typing = composeTestRule.activity.getString(R.string.kaleyra_chat_participants_online, users.size, users.size)
        getSubtitle().assertContentDescriptionEquals(typing)
    }

    @Test
    fun defaultSubtitle_usernamesAreDisplayed() {
        participantsDetails = ImmutableMap(
                mapOf(
                    "userId1" to ChatParticipantDetails("John Smith"),
                    "userId2" to ChatParticipantDetails("Jack Daniels")
                )
            )
        val subtitle = participantsDetails.value.values.joinToString(", ") { it.username }
        getSubtitle().assertContentDescriptionEquals(subtitle)
    }

    @Test
    fun participantsStateTyping_typingDotsDisplayed() {
        getBouncingDots().assertDoesNotExist()
        val users = listOf("mary", "john")
        participantsState = ChatParticipantsState(typing = ImmutableList(users))
        getBouncingDots().assertIsDisplayed()
    }

    @Test
    fun isInCallTrue_actionsAreNotDisplayed() {
        composeTestRule.onNodeWithTag(ChatActionsTag).assertIsDisplayed()
        isInCall = true
        composeTestRule.onNodeWithTag(ChatActionsTag).assertDoesNotExist()
    }

    @Test
    fun userClicksAction_onActionClickedInvoked() {
        composeTestRule.onNodeWithTag(ActionsTag).onChildren().onFirst().performClick()
        assert(isActionClicked)
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(isBackPressed)
    }

    private fun getSubtitle() = composeTestRule.onNodeWithTag(SubtitleTag)

    private fun getBouncingDots() = composeTestRule.onNodeWithTag(BouncingDotsTag)
}