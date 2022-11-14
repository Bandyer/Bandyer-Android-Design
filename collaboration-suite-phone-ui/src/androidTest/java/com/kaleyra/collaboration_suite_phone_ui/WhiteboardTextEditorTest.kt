package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.TextEditorState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.WhiteboardTextEditor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhiteboardTextEditorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var state by mutableStateOf(TextEditorState.Empty)

    private var text = ""

    private var dismissClicked = false

    private var confirmClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardTextEditor(
                state = state,
                textFieldValue = TextFieldValue(),
                onTextChanged = { text = it.text },
                onDismissClick = { dismissClicked = true },
                onConfirmClick = { confirmClicked = true }
            )
        }
        dismissClicked = false
    }

    @Test
    fun emptyState_dismissButtonDisplayed() {
        state = TextEditorState.Empty
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).assertIsDisplayed()
        composeTestRule.onNodeWithText(dismiss).assertIsDisplayed()
    }

    @Test
    fun editingState_discardButtonDisplayed() {
        state = TextEditorState.Editing
        val discard = composeTestRule.activity.getString(R.string.kaleyra_action_discard_changes)
        composeTestRule.onNodeWithContentDescription(discard).assertIsDisplayed()
        composeTestRule.onNodeWithText(discard).assertIsDisplayed()
    }

    @Test
    fun discardState_cancelButtonDisplayed() {
        state = TextEditorState.Discard
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_action_cancel)
        composeTestRule.onNodeWithContentDescription(cancel).assertIsDisplayed()
        composeTestRule.onNodeWithText(cancel).assertIsDisplayed()
    }

    @Test
    fun discardState_confirmDiscardChangesDisplayed() {
        state = TextEditorState.Discard
        val confirmDiscard = composeTestRule.activity.getString(R.string.kaleyra_data_loss_confirm_message)
        composeTestRule.onNodeWithText(confirmDiscard).assertIsDisplayed()
    }

    @Test
    fun discardState_editTextNotDisplayed() {
        state = TextEditorState.Discard
        composeTestRule.onNode(hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun emptyState_userClicksDismiss_onDismissClickInvoked() {
        state = TextEditorState.Empty
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).performClick()
        assert(dismissClicked)
    }

    @Test
    fun editingState_userClicksDismiss_onDismissClickInvoked() {
        state = TextEditorState.Editing
        val discard = composeTestRule.activity.getString(R.string.kaleyra_action_discard_changes)
        composeTestRule.onNodeWithContentDescription(discard).performClick()
        assert(dismissClicked)
    }

    @Test
    fun discardState_userClicksDismiss_onDismissClickInvoked() {
        state = TextEditorState.Discard
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_action_cancel)
        composeTestRule.onNodeWithContentDescription(cancel).performClick()
        assert(dismissClicked)
    }

    @Test
    fun userClicksConfirm_onConfirmClickInvoked() {
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assert(confirmClicked)
    }

    @Test
    fun userTypesText_onTextChangedInvoked() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Text")
        assertEquals("Text", text)
    }

}