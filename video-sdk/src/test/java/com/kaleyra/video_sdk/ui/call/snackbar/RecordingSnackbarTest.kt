package com.kaleyra.video_sdk.ui.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.snackbar.RecordingEndedSnackbar
import com.kaleyra.video_sdk.common.snackbar.RecordingErrorSnackbar
import com.kaleyra.video_sdk.common.snackbar.RecordingStartedSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecordingSnackbarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testRecordingStartedSnackbar() {
        composeTestRule.setContent { RecordingStartedSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun testRecordingEndedSnackbar() {
        composeTestRule.setContent { RecordingEndedSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_stopped)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_stopped_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun testRecordingErrorSnackbar() {
        composeTestRule.setContent { RecordingErrorSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_failed)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_failed_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }
}