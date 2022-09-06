package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.ChatScreen
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val uiState = MutableStateFlow(ChatUiState())

    private var isBackPressed = false

    private var showCall = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ChatScreen(
                uiState = uiState.collectAsState().value,
                onBackPressed = { isBackPressed = true },
                onMessageScrolled = { /*TODO*/ },
                onResetMessagesScroll = { /*TODO*/ },
                onFetchMessages = { /*TODO*/ },
                onReadAllMessages = { /*TODO*/ },
                onCall = { /*TODO*/ },
                onShowCall = { showCall = true },
                onSendMessage = { },
                onTyping = { }
            )
        }
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onNodeWithContentDescription(back).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userIsInCall_ongoingCallAppears() {
        val ongoingCall = composeTestRule.activity.getString(R.string.kaleyra_ongoing_call_label)
        composeTestRule.onNodeWithText(ongoingCall).assertDoesNotExist()
        uiState.update { it.copy(isInCall = true) }
        composeTestRule.onNodeWithText(ongoingCall).assertIsDisplayed()
    }

    @Test
    fun userClicksOngoingCall_onShowCallInvoked() {
        val ongoingCall = composeTestRule.activity.getString(R.string.kaleyra_ongoing_call_label)
        uiState.update { it.copy(isInCall = true) }
        composeTestRule.onNodeWithText(ongoingCall).performClick()
        assert(showCall)
    }

}