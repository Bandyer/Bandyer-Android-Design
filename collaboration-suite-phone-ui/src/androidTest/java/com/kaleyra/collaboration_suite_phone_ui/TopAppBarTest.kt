package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.chat.topappbar.*
import kotlinx.coroutines.flow.MutableStateFlow
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
class TopAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val chatState = MutableStateFlow<ChatState>(ChatState.None)

    private val chatInfo = ChatInfo(name = "chatName")

    private val chatActions = ImmutableSet(setOf<ChatAction>(ChatAction.AudioCall { isActionClicked = true }))

    private var isBackPressed = false

    private var isActionClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ChatAppBar(
                state = chatState.collectAsState().value,
                info = chatInfo,
                actions = chatActions,
                onBackPressed = { isBackPressed = true })
        }
    }

    @Test
    fun title_set() {
        // Check all nodes with the given text and get the first one. This is done because of the MarqueeText implementation.
        composeTestRule.onAllNodesWithText(chatInfo.name).onFirst().assertIsDisplayed()
    }

    @Test
    fun chatStateNone_subtitleNotDisplayed() {
        getSubtitle().assertTextEquals("")
    }

    @Test
    fun chatStateNetworkConnecting_connectingDisplayed() {
        chatState.value = ChatState.NetworkState.Connecting
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_chat_state_connecting)
        getSubtitle().assertTextEquals(connecting)
    }

    @Test
    fun chatStateNetworkOffline_waitingForNetworkDisplayed() {
        chatState.value = ChatState.NetworkState.Offline
        val waitingForNetwork = composeTestRule.activity.getString(R.string.kaleyra_chat_state_waiting_for_network)
        getSubtitle().assertTextEquals(waitingForNetwork)
    }

    @Test
    fun chatStateUserOnline_onlineDisplayed() {
        chatState.value = ChatState.UserState.Online
        val online = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_online)
        getSubtitle().assertTextEquals(online)
    }

    @Test
    fun chatStateUserOffline_lastLoginDisplayed() {
        chatState.value = ChatState.UserState.Offline(0)
        val timestamp = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(0))
        val offline = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_last_login, timestamp)
        getSubtitle().assertTextEquals(offline)
    }

    @Test
    fun chatStateUserNeverOnline_recentlySeenDisplayed() {
        chatState.value = ChatState.UserState.Offline(null)
        val offline = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_offline)
        getSubtitle().assertTextEquals(offline)
    }

    @Test
    fun chatStateUserTyping_typingWithDotsDisplayed() {
        getBouncingDots().assertDoesNotExist()
        chatState.value = ChatState.UserState.Typing
        val typing = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_typing)
        getSubtitle().assertTextEquals(typing)
        getBouncingDots().assertIsDisplayed()
    }

    @Test
    fun userClicksAction_onActionClickedInvoked() {
        composeTestRule.onNodeWithTag(ActionsTag).onChildren().onFirst().performClick()
        assert(isActionClicked)
    }

    // Check all nodes with the given tag and get the first one. This is done because of the MarqueeText implementation.
    private fun getSubtitle() = composeTestRule.onAllNodesWithTag(SubtitleTag).onFirst()

    private fun getBouncingDots() = composeTestRule.onNodeWithTag(BouncingDots)
}