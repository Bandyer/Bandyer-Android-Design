package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.input.UserInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserInputTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var onTextChanged = false

    private var onSendMessage = false
    
    @Before
    fun setUp() {
        composeTestRule.setContent {
            UserInput(
                onTextChanged = {
                    onTextChanged = true
                },
                onMessageSent = {
                    onSendMessage = true
                }
            )
        }
    }

    @Test
    fun userTypesText_enableSendButton() {
        findSendButton().assertIsNotEnabled()
        findTextInputField().performTextInput("Text")
        findSendButton().assertIsEnabled()
    }

    @Test
    fun userFocusesInputField_hintIsStillDisplayed() {
        findHintText().assertIsDisplayed()
        findTextInputField().performClick()
        findHintText().assertIsDisplayed()
    }

    @Test
    fun userTypesText_hintDisappear() {
        findHintText().assertIsDisplayed()
        findTextInputField().performTextInput("Text")
        findHintText().assertDoesNotExist()
    }

    @Test
    fun userTypesText_onTextChangedInvoked() {
        findTextInputField().performTextInput("Text")
        assert(onTextChanged)
    }

    @Test
    fun userClicksButton_onMessageSentInvoked() {
        findTextInputField().performTextInput("Text")
        findSendButton().performClick()
        assert(onSendMessage)
    }

    private fun findSendButton() =
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_send))

    private fun findHintText() =
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_edit_text_input_placeholder))

    private fun findTextInputField() = composeTestRule.onNode(
        hasSetTextAction() and hasAnyAncestor(
            hasContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_textfield_desc))
        )
    )

}