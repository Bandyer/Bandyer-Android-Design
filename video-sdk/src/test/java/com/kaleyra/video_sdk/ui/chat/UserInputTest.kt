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

package com.kaleyra.video_sdk.ui.chat

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.input.TextFieldTag
import com.kaleyra.video_sdk.chat.input.ChatUserInput
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class UserInputTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var onTextChanged = false

    private var onSendMessage = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ChatUserInput(
                onTextChanged = {
                    onTextChanged = true
                },
                onMessageSent = {
                    onSendMessage = true
                }
            )
        }
    }

    @After
    fun tearDown() {
        onTextChanged = false
        onSendMessage = false
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

    @Test
    fun userClicksButton_inputFieldCleaned() {
        findTextInputField().performTextInput("Text")
        findSendButton().performClick()
        composeTestRule.onNode(hasText("Text")).assertDoesNotExist()
    }

    private fun findSendButton() =
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_chat_send))

    private fun findHintText() =
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_edit_text_input_placeholder))

    private fun findTextInputField() = composeTestRule.onNode(hasTestTag(TextFieldTag))

}