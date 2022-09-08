package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.ChatScreen
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.MessagesTag
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.mockActions
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.mockConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.ActionsTag
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ConversationUiState
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

    private val uiState = MutableStateFlow(
        ChatUiState(
            conversationState = ConversationUiState(
                areMessagesInitialized = true,
                conversationItems = mockConversationItems.plus(mockConversationItems)
            ),
            actions = mockActions
        )
    )

    private var onBackPressed = false

    private var onMessageScrolled = false

    private var onResetMessagesScroll = false

    private var onFetchMessages = false

    private var onReadAllMessages = false

    private var onCall = false

    private var onShowCall = false

    private var onSendMessage = false

    private var onTyping = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ChatScreen(
                uiState = uiState.collectAsState().value,
                onBackPressed = { onBackPressed = true },
                onMessageScrolled = { onMessageScrolled = true },
                onResetMessagesScroll = { onResetMessagesScroll = true },
                onFetchMessages = { onFetchMessages = true },
                onReadAllMessages = { onReadAllMessages = true },
                onCall = { onCall = true },
                onShowCall = { onShowCall = true },
                onSendMessage = { onSendMessage = true },
                onTyping = { onTyping = true }
            )
        }
    }

    // Missing
    // onReadAllMessages

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onNodeWithContentDescription(back).performClick()
        assert(onBackPressed)
    }

    @Test
    fun userScrollsUp_onMessageScrolledInvoked() {
        findMessages().performScrollUp()
        assert(onMessageScrolled)
    }

    @Test
    fun userClicksFab_resetScrollInvoked() {
        findMessages().performScrollUp()
        findResetScrollFab().performClick()
        findResetScrollFab().assertDoesNotExist()
        assert(onResetMessagesScroll)
    }

    @Test
    fun userScrollsToTop_onFetchMessagesInvoked() {
        findMessages().performScrollUp()
        findMessages().performScrollUp()
        assert(onFetchMessages)
    }

    @Test
    fun userIsInCall_ongoingCallAppears() {
        val ongoingCall = composeTestRule.activity.getString(R.string.kaleyra_ongoing_call_label)
        composeTestRule.onNodeWithText(ongoingCall).assertDoesNotExist()
        uiState.update { it.copy(isInCall = true) }
        composeTestRule.onNodeWithText(ongoingCall).assertIsDisplayed()
    }

    @Test
    fun userClicksAction_onCallInvoked() {
        composeTestRule.onNodeWithTag(ActionsTag).onChildren().onFirst().performClick()
        assert(onCall)
    }

    @Test
    fun userClicksOngoingCall_onShowCallInvoked() {
        val ongoingCall = composeTestRule.activity.getString(R.string.kaleyra_ongoing_call_label)
        uiState.update { it.copy(isInCall = true) }
        composeTestRule.onNodeWithText(ongoingCall).performClick()
        assert(onShowCall)
    }

    @Test
    fun userClicksButton_onMessageSentInvoked() {
        findTextInputField().performTextInput("Text")
        findSendButton().performClick()
        assert(onSendMessage)
    }

    @Test
    fun userTypesText_onTypingInvoked() {
        findTextInputField().performTextInput("Text")
        assert(onTyping)
    }

    private fun findMessages() = composeTestRule.onNodeWithTag(MessagesTag)

    private fun findResetScrollFab() = composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_scroll_to_last_message))

    private fun findSendButton() = composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_send))

    private fun findTextInputField() = composeTestRule.onNode(
        hasSetTextAction() and hasAnyAncestor(
            hasContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_textfield_desc))
        )
    )
}