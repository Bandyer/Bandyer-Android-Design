package com.kaleyra.collaboration_suite_phone_ui.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.common.snackbar.AudioOutputGenericFailureSnackbar
import com.kaleyra.collaboration_suite_phone_ui.common.snackbar.AudioOutputInSystemCallFailureSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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