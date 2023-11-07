package com.kaleyra.video_sdk.ui.call

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.recording.view.RecordingLabel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*


@RunWith(RobolectricTestRunner::class)
class RecordingLabelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            RecordingLabel()
        }
    }

    @Test
    fun recTextIsDisplayed() {
        val rec = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec).uppercase()
        composeTestRule.onNodeWithText(rec, ignoreCase = true).assertIsDisplayed()
    }
}