package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.*
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.BouncingDotsTag
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.ChatActionsTag
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.GroupAppBar
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.SubtitleTag
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.common.topappbar.ActionsTag
import com.kaleyra.collaboration_suite_phone_ui.findBackButton
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

    private var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Undefined)

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
        connectionState = ConnectionState.Undefined
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