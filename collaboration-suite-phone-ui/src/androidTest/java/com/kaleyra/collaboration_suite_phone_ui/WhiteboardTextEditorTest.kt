package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.TextEditorState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.TextEditorValue
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardTextEditor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhiteboardTextEditorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var textEditorState by mutableStateOf(TextEditorState(initialValue = TextEditorValue.Empty))

    private var dismissClicked = false

    private var confirmClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardTextEditor(
                textEditorState = textEditorState,
                onDismissClick = { dismissClicked = true },
                onConfirmClick = { confirmClicked = true }
            )
        }
        dismissClicked = false
        confirmClicked = false
    }

    @Test
    fun emptyState_dismissButtonDisplayed() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Empty)
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).assertIsDisplayed()
        composeTestRule.onNodeWithText(dismiss).assertIsDisplayed()
    }

    @Test
    fun editingState_discardButtonDisplayed() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Editing(TextFieldValue("text")))
        val discard = composeTestRule.activity.getString(R.string.kaleyra_action_discard_changes)
        composeTestRule.onNodeWithContentDescription(discard).assertIsDisplayed()
        composeTestRule.onNodeWithText(discard).assertIsDisplayed()
    }

    @Test
    fun discardState_cancelButtonDisplayed() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Discard)
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_action_cancel)
        composeTestRule.onNodeWithContentDescription(cancel).assertIsDisplayed()
        composeTestRule.onNodeWithText(cancel).assertIsDisplayed()
    }

    @Test
    fun discardState_confirmDiscardChangesDisplayed() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Discard)
        val confirmDiscard = composeTestRule.activity.getString(R.string.kaleyra_data_loss_confirm_message)
        composeTestRule.onNodeWithText(confirmDiscard).assertIsDisplayed()
    }

    @Test
    fun discardState_editTextNotDisplayed() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Discard)
        composeTestRule.onNode(hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun emptyState_userClicksDismiss_onDismissClickInvoked() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Empty)
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).performClick()
        assert(dismissClicked)
    }

    @Test
    fun editingState_userClicksDiscardChanges_enterDiscardState() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Editing(TextFieldValue("text")))
        val discard = composeTestRule.activity.getString(R.string.kaleyra_action_discard_changes)
        composeTestRule.onNodeWithContentDescription(discard).performClick()
        assertEquals(TextEditorValue.Discard, textEditorState.currentValue)
    }

    @Test
    fun discardState_userClicksCancel_enterEditingState() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Discard)
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_action_cancel)
        composeTestRule.onNodeWithContentDescription(cancel).performClick()
        assertEquals(TextEditorValue.Editing::class.java, textEditorState.currentValue::class.java)
    }

    @Test
    fun emptyText_userClicksConfirm_onDismissClickInvoked() {
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assert(dismissClicked)
    }

    @Test
    fun textTyped_userClicksConfirm_onConfirmClickInvoked() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Text")
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assert(confirmClicked)
    }

    @Test
    fun userTypesText_textEditorEntersEditingState() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Text")
        val textFieldValue = (textEditorState.currentValue as? TextEditorValue.Editing)?.textFieldValue
        assertEquals("Text", textFieldValue!!.text)
        assertEquals(textEditorState.textFieldValue, textFieldValue)
    }

}