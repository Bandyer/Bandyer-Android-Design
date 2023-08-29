package com.kaleyra.collaboration_suite_phone_ui.ui.call.whiteboard

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.LinearProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardViewTag
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
class WhiteboardContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var loading by mutableStateOf(false)

    private var upload by mutableStateOf<WhiteboardUploadUi?>(null)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardContent(
                whiteboardView = View(composeTestRule.activity),
                loading = loading,
                upload = upload
            )
        }
    }

    @After
    fun tearDown() {
        loading = false
        upload = null
    }

    @Test
    fun whiteboardViewIsDisplayed() {
        composeTestRule.onNodeWithTag(WhiteboardViewTag).assertIsDisplayed()
    }

    @Test
    fun whiteboardUploadError_errorCardDisplayed() {
        upload = null
        val title = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_error_title)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_error_subtitle)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        upload = WhiteboardUploadUi.Error
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun whiteboardUploadUploading_uploadingCardDisplayed() {
        upload = null
        val title = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_uploading_file)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_compressing)
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_file_upload_percentage, 70)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        composeTestRule.onNodeWithText(percentage).assertDoesNotExist()
        upload = WhiteboardUploadUi.Uploading(.7f)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun loadingTrue_indeterminateProgressIndicatorDisplayed() {
        loading = true
        composeTestRule
            .onNodeWithTag(LinearProgressIndicatorTag)
            .assertIsDisplayed()
            .assertRangeInfoEquals(ProgressBarRangeInfo.Indeterminate)
    }

    @Test
    fun loadingFalse_indeterminateProgressIndicatorDoesNotExists() {
        loading = false
        composeTestRule
            .onNodeWithTag(LinearProgressIndicatorTag)
            .assertDoesNotExist()
    }
}