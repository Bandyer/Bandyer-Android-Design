package com.kaleyra.collaboration_suite_phone_ui.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

@RunWith(AndroidJUnit4::class)
class WhiteboardComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var uiState by mutableStateOf(WhiteboardUiState())

    private var isReloadClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardComponent(
                uiState = uiState,
                onReloadClick = { isReloadClicked = true },
                onTextConfirm = { },
                onTextDismiss = {},
                onWhiteboardViewCreated = {},
                onWhiteboardViewDispose = {},
            )
        }
    }

    // test per onTextEditorDismiss
    // test per onTextConfirmed
    // test per onWhiteboardViewCreated
    // test per onWhiteboardViewDispose

    @Test
    fun textNull_whiteboardIsDisplayed() {
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        uiState = WhiteboardUiState(text = null)
        composeTestRule.onNodeWithTag(WhiteboardViewTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(confirm).assertIsNotDisplayed()
    }

    @Test
    fun textNotNull_textEditorIsDisplayed() {
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