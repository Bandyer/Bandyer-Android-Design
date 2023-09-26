package com.kaleyra.collaboration_suite_phone_ui.ui.call.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.TextEditorState
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.TextEditorValue
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.WhiteboardModalBottomSheetContent
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