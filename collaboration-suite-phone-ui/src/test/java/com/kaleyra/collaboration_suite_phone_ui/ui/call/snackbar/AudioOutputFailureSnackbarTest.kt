package com.kaleyra.collaboration_suite_phone_ui.ui.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.AudioOutputGenericFailureSnackbar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.AudioOutputInSystemCallFailureSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AudioOutputFailureSnackbarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testGenericAudioFailureSnackbar() {
        composeTestRule.setContent { AudioOutputGenericFailureSnackbar() }
        val text = composeTestRule.activity.resources.getString(R.string.kaleyra_generic_audio_routing_error)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun tesInSystemCallAudioFailureSnackbar() {
        composeTestRule.setContent { AudioOutputInSystemCallFailureSnackbar() }
        val text = composeTestRule.activity.resources.getString(R.string.kaleyra_already_in_system_call_audio_routing_error)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}