package com.kaleyra.collaboration_suite_phone_ui.ui.call.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.TextEditorState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.TextEditorValue
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardTextEditor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
class WhiteboardTextEditorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var textEditorState by mutableStateOf(TextEditorState(initialValue = TextEditorValue.Empty))

    private var onDismissInvoked = false

    private var textConfirmed: String? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardTextEditor(
                textEditorState = textEditorState,
                onDismiss = { onDismissInvoked = true },
                onConfirm = { textConfirmed = it }
            )
        }
    }

    @After
    fun tearDown() {
        textEditorState = TextEditorState(TextEditorValue.Empty)
        onDismissInvoked = false
        textConfirmed = null
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
    fun emptyState_userClicksDismiss_onDismissInvoked() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Empty)
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).performClick()
        assert(onDismissInvoked)
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
    fun emptyText_userClicksConfirm_onDismissInvoked() {
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assert(onDismissInvoked)
    }

    @Test
    fun editingState_userClicksConfirm_onConfirmInvokedWithText() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Editing(TextFieldValue("text")))
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals("text", textConfirmed)
    }

    @Test
    fun emptyState_userClicksConfirm_onConfirmInvokedWithNullText() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Empty)
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals(null, textConfirmed)
    }

    @Test
    fun discardState_userClicksConfirm_onConfirmInvokedWithNullText() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Discard)
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals(null, textConfirmed)
    }

    @Test
    fun userTypesText_textEditorIsInEditingState() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Text")
        val textFieldValue = (textEditorState.currentValue as? TextEditorValue.Editing)?.textFieldValue
        assertEquals("Text", textFieldValue!!.text)
        assertEquals(textEditorState.textFieldValue, textFieldValue)
    }
    @Test
    fun emptyState_userClicksDismiss_editorStateIsCleared() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Empty)
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).performClick()
        assertEquals(TextEditorValue.Empty, textEditorState.currentValue)
        assertEquals(TextFieldValue(), textEditorState.textFieldValue)
    }

    @Test
    fun emptyState_userClicksConfirm_editorStateIsCleared() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Empty)
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals(TextEditorValue.Empty, textEditorState.currentValue)
        assertEquals(TextFieldValue(), textEditorState.textFieldValue)
    }

    @Test
    fun editingState_userClicksConfirm_editorStateIsCleared() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Editing(TextFieldValue("text")))
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals(TextEditorValue.Empty, textEditorState.currentValue)
        assertEquals(TextFieldValue(), textEditorState.textFieldValue)
    }

    @Test
    fun discardState_userClicksConfirm_editorStateIsCleared() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Discard)
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals(TextEditorValue.Empty, textEditorState.currentValue)
        assertEquals(TextFieldValue(), textEditorState.textFieldValue)
    }

    @Test
    fun emptyState_clearState_editorStateIsCleared() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Empty)
        textEditorState.clearState()
        assertEquals(TextEditorValue.Empty, textEditorState.currentValue)
        assertEquals(TextFieldValue(), textEditorState.textFieldValue)
    }

    @Test
    fun editingState_clearState_editorStateIsCleared() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Editing(TextFieldValue("text")))
        textEditorState.clearState()
        assertEquals(TextEditorValue.Empty, textEditorState.currentValue)
        assertEquals(TextFieldValue(), textEditorState.textFieldValue)
    }

    @Test
    fun discardState_clearState_editorStateIsCleared() {
        textEditorState = TextEditorState(initialValue = TextEditorValue.Discard)
        textEditorState.clearState()
        assertEquals(TextEditorValue.Empty, textEditorState.currentValue)
        assertEquals(TextFieldValue(), textEditorState.textFieldValue)
    }

}