package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.chat.topappbar.*
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
class ChatAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var chatState by mutableStateOf<ChatState>(ChatState.None)

    private var isInCall by mutableStateOf(false)

    private val chatInfo = ChatInfo(name = "chatName")

    private val chatActions = ImmutableSet(setOf<ChatAction>(ChatAction.AudioCall { isActionClicked = true }))

    private var isBackPressed = false

    private var isActionClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ChatAppBar(
                state = chatState,
                info = chatInfo,
                isInCall = isInCall,
                actions = chatActions,
                onBackPressed = { isBackPressed = true })
        }
    }

    @After
    fun tearDown() {
        chatState = ChatState.None
        isInCall = false
        isBackPressed = false
        isActionClicked = false
    }

    // Check the content description instead of the text because the title and subtitle views are AndroidViews

    @Test
    fun title_set() {
        composeTestRule.onNodeWithContentDescription(chatInfo.name).assertIsDisplayed()
    }

    @Test
    fun chatStateNone_subtitleNotDisplayed() {
        getSubtitle().assertContentDescriptionEquals("")
    }

    @Test
    fun chatStateNetworkConnecting_connectingDisplayed() {
        chatState = ChatState.NetworkState.Connecting
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_chat_state_connecting)
        getSubtitle().assertContentDescriptionEquals(connecting)
    }

    @Test
    fun chatStateNetworkOffline_waitingForNetworkDisplayed() {
        chatState = ChatState.NetworkState.Offline
        val waitingForNetwork = composeTestRule.activity.getString(R.string.kaleyra_chat_state_waiting_for_network)
        getSubtitle().assertContentDescriptionEquals(waitingForNetwork)
    }

    @Test
    fun chatStateUserOnline_onlineDisplayed() {
        chatState = ChatState.UserState.Online
        val online = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_online)
        getSubtitle().assertContentDescriptionEquals(online)
    }

    @Test
    fun chatStateUserOffline_lastLoginDisplayed() {
        chatState = ChatState.UserState.Offline(0)
        val timestamp = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(0))
        val offline = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_last_login, timestamp)
        getSubtitle().assertContentDescriptionEquals(offline)
    }

    @Test
    fun chatStateUserNeverOnline_recentlySeenDisplayed() {
        chatState = ChatState.UserState.Offline(null)
        val offline = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_offline)
        getSubtitle().assertContentDescriptionEquals(offline)
    }

    @Test
    fun chatStateUserTyping_typingWithDotsDisplayed() {
        getBouncingDots().assertDoesNotExist()
        chatState = ChatState.UserState.Typing
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

    private fun getSubtitle() = composeTestRule.onNodeWithTag(SubtitleTag)

    private fun getBouncingDots() = composeTestRule.onNodeWithTag(BouncingDotsTag)
}