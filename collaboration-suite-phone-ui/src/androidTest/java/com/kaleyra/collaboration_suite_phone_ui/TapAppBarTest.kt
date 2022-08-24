package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.ActionsTag
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.ClickableAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.TopAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TapAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val chatState = MutableStateFlow<ChatState>(ChatState.None)

    private val chatInfo = MutableStateFlow(ChatInfo(name = "chatName"))

    private val actions = MutableStateFlow(
        setOf(ClickableAction(ChatAction.AudioCall) { isActionClicked = true })
    )

    private var isBackPressed = false

    private var isActionClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            TopAppBar(
                state = chatState.collectAsState().value,
                info = chatInfo.collectAsState().value,
                actions = actions.collectAsState().value,
                onBackPressed = { isBackPressed = true })
        }
    }

    @Test
    fun title_set() {
        Espresso.onView(withText(chatInfo.value.name)).check(matches(isDisplayed()))
    }

    @Test
    fun chatStateNone_subtitleNotDisplayed() {
        Espresso.onView(withId(R.id.kaleyra_subtitle_text)).check(matches(withText("")))
    }

    @Test
    fun chatStateNetworkConnecting_connectingDisplayed() {
        chatState.value = ChatState.NetworkState.Connecting
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_chat_state_connecting)
        Espresso.onView(withId(R.id.kaleyra_subtitle_text)).check(matches(withText(connecting)))
    }

    @Test
    fun chatStateNetworkOffline_waitingForNetworkDisplayed() {
        chatState.value = ChatState.NetworkState.Offline
        val waitingForNetwork = composeTestRule.activity.getString(R.string.kaleyra_chat_state_waiting_for_network)
        Espresso.onView(withId(R.id.kaleyra_subtitle_text)).check(matches(withText(waitingForNetwork)))
    }

    @Test
    fun chatStateUserOnline_onlineDisplayed() {
        chatState.value = ChatState.UserState.Online
        val online = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_online)
        Espresso.onView(withId(R.id.kaleyra_subtitle_text)).check(matches(withText(online)))
    }

    @Test
    fun chatStateUserOffline_offlineDisplayed() {
        val timestamp = "16:22"
        chatState.value = ChatState.UserState.Offline(timestamp)
        val lastLogin = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_last_login)
        Espresso.onView(withId(R.id.kaleyra_subtitle_text)).check(matches(withText("$lastLogin $timestamp")))
    }

    @Test
    fun chatStateUserTyping_typingWithDotsDisplayed() {
        Espresso.onView(withId(R.id.kaleyra_subtitle_bouncing_dots)).check(matches(not(isDisplayed())))
        chatState.value = ChatState.UserState.Typing
        val typing = composeTestRule.activity.getString(R.string.kaleyra_chat_user_status_typing)
        Espresso.onView(withId(R.id.kaleyra_subtitle_text)).check(matches(withText(typing)))
        Espresso.onView(withId(R.id.kaleyra_subtitle_bouncing_dots)).check(matches(isDisplayed()))
    }

    @Test
    fun userClicksAction_onActionClickedInvoked() {
        composeTestRule.onNodeWithTag(ActionsTag).onChildren().onFirst().performClick()
        assert(isActionClicked)
    }
}