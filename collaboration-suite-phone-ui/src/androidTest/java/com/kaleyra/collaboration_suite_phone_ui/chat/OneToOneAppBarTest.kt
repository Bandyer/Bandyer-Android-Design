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
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.BouncingDotsTag
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.ChatActionsTag
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.OneToOneAppBar
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.view.SubtitleTag
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.common.topappbar.ActionsTag
import com.kaleyra.collaboration_suite_phone_ui.findBackButton
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@RunWith(AndroidJUnit4::class)
class OneToOneAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val chatParticipantState = MutableStateFlow<ChatParticipantState>(ChatParticipantState.Unknown)

    private var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Unknown)

    private var chatParticipantsDetails by mutableStateOf(ChatParticipantDetails(username = "recipientUser", state = chatParticipantState))

    private var isInCall by mutableStateOf(false)

    private val chatActions = ImmutableSet(setOf<ChatAction>(ChatAction.AudioCall { isActionClicked = true }))

    private var isBackPressed = false

    private var isActionClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            OneToOneAppBar(
                connectionState = connectionState,
                recipientDetails = chatParticipantsDetails,
                isInCall = isInCall,
                actions = chatActions,
                onBackPressed = { isBackPressed = true }
            )
        }
    }

    @After
    fun tearDown() {
        connectionState = ConnectionState.Unknown
        ChatParticipantDetails(username = "recipientUser", state = chatParticipantState)
        chatParticipantState.value = ChatParticipantState.Unknown
        isInCall = false
        isBackPressed = false
        isActionClicked = false
    }

    // Check the content description instead of the text because the title and subtitle views are AndroidViews
    @Test
    fun title_set() {
        composeTestRule.onNodeWithContentDescription(chatParticipantsDetails.username).assertIsDisplayed()
    }

    @Test
    fun connectionStateUndefinedAndRecipientStateUnknown_subtitleNotDisplayed() {
        connectionState = ConnectionState.Unknown
        chatParticipantState.value = ChatParticipantState.Unknown
        getSubtitle().assertContentDescriptionEquals("")
    }

    @Test
    fun connectionStateConnecting_connectingDisplayed() {
        connectionState = ConnectionState.Connecting
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_chat_state_connecting)
        getSubtitle().assertContentDescriptionEquals(connecting)
    }

    @Test
    fun connectionStateOffline_waitingForNetworkDisplayed() {
        connectionState = ConnectionState.Offline
        val waitingForNetwork = composeTestRule.activity.getString(R.string.kaleyra_chat_state_waiting_for_network)
        getSubtitle().assertContentDescriptionEquals(waitingForNetwork)
    }

    @Test
    fun chatParticipantStateOnline_onlineDisplayed() {
        chatParticipantState.value = ChatParticipantState.Online
        val online = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_online)
        getSubtitle().assertContentDescriptionEquals(online)
    }

    @Test
    fun chatParticipantStateOffline_lastLoginDisplayed() {
        chatParticipantState.value = ChatParticipantState.Offline(0)
        val timestamp = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(0))
        val offline = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_last_login, timestamp)
        getSubtitle().assertContentDescriptionEquals(offline)
    }

    @Test
    fun chatParticipantStateNeverOnline_recentlySeenDisplayed() {
        chatParticipantState.value = ChatParticipantState.Offline(null)
        val offline = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_offline)
        getSubtitle().assertContentDescriptionEquals(offline)
    }

    @Test
    fun chatParticipantStateTyping_typingWithDotsDisplayed() {
        getBouncingDots().assertDoesNotExist()
        chatParticipantState.value = ChatParticipantState.Typing
        val typing = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_typing)
        getSubtitle().assertContentDescriptionEquals(typing)
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