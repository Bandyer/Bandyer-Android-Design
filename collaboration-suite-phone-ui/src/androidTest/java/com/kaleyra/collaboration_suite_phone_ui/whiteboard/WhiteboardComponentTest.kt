package com.kaleyra.collaboration_suite_phone_ui.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite.phonebox.WhiteboardView
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.WhiteboardComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.LinearProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardViewTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals

@OptIn(ExperimentalMaterialApi::class)
@RunWith(AndroidJUnit4::class)
class WhiteboardComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var uiState by mutableStateOf(WhiteboardUiState())

    private var sheetState by mutableStateOf(ModalBottomSheetState(ModalBottomSheetValue.Hidden))

    private var isReloadClicked = false

    private var confirmedText: String? = null

    private var isTextDismissed = false

    private var whiteboardView: WhiteboardView? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardComponent(
                uiState = uiState,
                editorSheetState = sheetState,
                onReloadClick = { isReloadClicked = true },
                onTextConfirm = { confirmedText = it },
                onTextDismiss = { isTextDismissed = true },
                onWhiteboardViewCreated = { whiteboardView = it },
                onWhiteboardViewDispose = { },
            )
        }
    }

    @Test
    fun textNull_whiteboardIsDisplayed() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        uiState = WhiteboardUiState(text = null)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(WhiteboardViewTag).assertIsDisplayed()
    }

    @Test
    fun textNull_sheetStateIsHidden() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        uiState = WhiteboardUiState(text = null)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(ModalBottomSheetValue.Hidden, currentValue)
        }
    }

    @Test
    fun textNotNull_textEditorIsDisplayed() {
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun textNotNull_sheetStateIsExpanded() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        uiState = WhiteboardUiState(text = "")
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(ModalBottomSheetValue.Expanded, currentValue)
        }
    }

    @Test
    fun userConfirmsEditorText_onTextConfirmedInvoked() {
        uiState = WhiteboardUiState(text = "text")
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals("text", confirmedText)
    }

    @Test
    fun userDismissesEditorText_onTextDismissInvoked() {
        // Set an empty text, so the editor is dismissed directly
        uiState = WhiteboardUiState(text = "")
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).performClick()
        assert(isTextDismissed)
    }

    @Test
    fun whiteboardViewComposableLaunched_onWhiteboardViewCreatedInvoked() {
        uiState = WhiteboardUiState(isOffline = false)
        composeTestRule.onNodeWithTag(WhiteboardViewTag).assertIsDisplayed()
        assert(whiteboardView != null)
    }

    @Test
    fun userDismissEditorText_onTextDismissInvoked() {
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun textEditorIsDisplayed_userClicksDismissButton_onTextDismissInvoked() {
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun offlineTrue_offlineUIDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_error_title)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_error_subtitle)
        val reload = composeTestRule.activity.getString(R.string.kaleyra_error_button_reload)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(reload).assertDoesNotExist()
        uiState =   WhiteboardUiState(isOffline = true)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(reload).assertIsDisplayed()
    }

    @Test
    fun userClicksReload_onReloadClickInvoked() {
        uiState = WhiteboardUiState(isOffline = true)
        val reload = composeTestRule.activity.getString(R.string.kaleyra_error_button_reload)
        composeTestRule.onNodeWithContentDescription(reload).performClick()
        assert(isReloadClicked)
    }

    @Test
    fun whiteboardUploadError_errorCardDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_error_title)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_error_subtitle)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        uiState = WhiteboardUiState(upload = WhiteboardUploadUi.Error)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun whiteboardUploadUploading_uploadingCardDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_uploading_file)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_compressing)
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_file_upload_percentage, 70)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        composeTestRule.onNodeWithText(percentage).assertDoesNotExist()
        uiState = WhiteboardUiState(upload = WhiteboardUploadUi.Uploading(.7f))
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun isLoadingTrue_indeterminateProgressIndicatorDisplayed() {
        uiState = WhiteboardUiState(isLoading = true, isOffline = false)
        composeTestRule
            .onNodeWithTag(LinearProgressIndicatorTag)
            .assertIsDisplayed()
            .assertRangeInfoEquals(ProgressBarRangeInfo.Indeterminate)
    }
}