package com.kaleyra.collaboration_suite_phone_ui.ui.chat

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.mockActions
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.collaboration_suite_phone_ui.chat.input.TextFieldTag
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.ChatScreen
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.ConversationComponentTag
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.ui.performScrollUp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val uiState = MutableStateFlow(
        ChatUiState(
            conversationState = ConversationUiState(
                conversationElements = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))
            ),
            actions = mockActions
        )
    )

    private var userMessage by mutableStateOf<UserMessage?>(null)

    private var onBackPressed = false

    private var onMessageScrolled = false

    private var onResetMessagesScroll = false

    private var onFetchMessages = false

    private var onShowCall = false

    private var onSendMessage = false

    private var onTyping = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ChatScreen(
                uiState = uiState.collectAsState().value,
                userMessage = userMessage,
                onBackPressed = { onBackPressed = true },
                onMessageScrolled = { onMessageScrolled = true },
                onResetMessagesScroll = { onResetMessagesScroll = true },
                onFetchMessages = { onFetchMessages = true },
                onShowCall = { onShowCall = true },
                onSendMessage = { onSendMessage = true },
                onTyping = { onTyping = true }
            )
        }
    }

    @After
    fun tearDown() {
        uiState.value = ChatUiState(conversationState = ConversationUiState(conversationElements = ImmutableList(mockConversationElements.value.plus(mockConversationElements.value))), actions = mockActions)
        onBackPressed = false
        onMessageScrolled = false
        onResetMessagesScroll = false
        onFetchMessages = false
        onShowCall = false
        onSendMessage = false
        onTyping = false
        userMessage = null
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onNodeWithContentDescription(back).performClick()
        assert(onBackPressed)
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

    @Test
    fun userMessage_userMessageSnackbarIsDisplayed() {
        userMessage = RecordingMessage.Started
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    private fun findMessages() = composeTestRule.onNodeWithTag(ConversationComponentTag)

    private fun findResetScrollFab() = composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(
        R.string.kaleyra_chat_scroll_to_last_message
    ))

    private fun findSendButton() = composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(
        R.string.kaleyra_chat_send
    ))

    private fun findTextInputField() = composeTestRule.onNode(hasTestTag(TextFieldTag))

}