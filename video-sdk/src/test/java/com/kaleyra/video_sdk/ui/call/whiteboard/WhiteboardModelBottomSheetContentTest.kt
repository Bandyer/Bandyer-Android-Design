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

package com.kaleyra.video_sdk.ui.call.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.whiteboard.view.TextEditorState
import com.kaleyra.video_sdk.call.whiteboard.view.TextEditorValue
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardModalBottomSheetContent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class WhiteboardModelBottomSheetContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var textEditorState by mutableStateOf(TextEditorState(TextEditorValue.Empty))

    private var isTextDismissed = false

    private var confirmedText: String? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardModalBottomSheetContent(
                textEditorState = textEditorState,
                onTextDismissed = { isTextDismissed = true },
                onTextConfirmed = { confirmedText = it }
            )
        }
    }

    @After
    fun tearDown() {
        textEditorState = TextEditorState(TextEditorValue.Empty)
        isTextDismissed = false
        confirmedText = null
    }

    @Test
    fun textIsDisplayed() {
        textEditorState = TextEditorState(TextEditorValue.Editing(TextFieldValue("text")))
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun userConfirmsText_onTextConfirmedInvoked() {
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNode(hasSetTextAction()).performTextInput("typed")
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals("typed", confirmedText)
    }

    @Test
    fun userDismissesText_onTextDismissInvoke() {
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).performClick()
        assert(isTextDismissed)
    }

}