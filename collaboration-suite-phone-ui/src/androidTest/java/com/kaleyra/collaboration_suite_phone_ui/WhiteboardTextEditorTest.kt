@file:OptIn(ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.WhiteboardTextEditor
import com.kaleyra.collaboration_suite_phone_ui.chat.input.TextFieldTag
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhiteboardTextEditorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var confirmedText: TextFieldValue? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardTextEditor(
                modalSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
                onConfirmClick = { confirmedText = it }
            )
        }
    }

    @Test
    fun userClicksConfirm_textFieldValueReceived() {
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        assertEquals(null, confirmedText)
        composeTestRule.onNode(hasTestTag(TextFieldTag)).performTextInput("Text")
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals("Text", confirmedText!!.text)
    }

    @Test
    fun emptyText_dismissButtonDisplayed() {
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).assertIsDisplayed()
        composeTestRule.onNodeWithText(dismiss).assertIsDisplayed()
    }

    @Test
    fun userTypesText_discardChangesButtonDisplayed() {
        val discardChanges = composeTestRule.activity.getString(R.string.kaleyra_action_discard_changes)
        composeTestRule.onNodeWithContentDescription(discardChanges).assertDoesNotExist()
        composeTestRule.onNodeWithText(discardChanges).assertDoesNotExist()
        composeTestRule.onNode(hasTestTag(TextFieldTag)).performTextInput("Text")
        composeTestRule.onNodeWithContentDescription(discardChanges).assertIsDisplayed()
        composeTestRule.onNodeWithText(discardChanges).assertIsDisplayed()
    }

    @Test
    fun userClicksDiscardChanges_confirmDiscardChangesDisplayed() {

    }

}