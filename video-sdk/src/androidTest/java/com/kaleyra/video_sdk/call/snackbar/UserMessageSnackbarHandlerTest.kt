package com.kaleyra.video_sdk.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.UserMessageSnackbarHandler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserMessageSnackbarHandlerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var userMessage by mutableStateOf<UserMessage?>(null)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            UserMessageSnackbarHandler(userMessage = userMessage)
        }
    }

    @Test
    fun recordingStartedUserMessage_recordingMessageIsDisplayed() {
        userMessage = RecordingMessage.Started
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun recordingStoppedUserMessage_recordingMessageIsDisplayed() {
        userMessage = RecordingMessage.Stopped
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_stopped)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_stopped_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun recordingFailedUserMessage_recordingMessageIsDisplayed() {
        userMessage = RecordingMessage.Failed
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_failed)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_failed_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun mutedUserMessage_mutedMessageIsDisplayed() {
        userMessage = MutedMessage(null)
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_muted_by_admin, 0, "")
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun usbConnectedUserMessage_usbMessageIsDisplayed() {
        userMessage = UsbCameraMessage.Connected("")
        val title = composeTestRule.activity.getString(R.string.kaleyra_generic_external_camera_connected)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun usbDisconnectedUserMessage_usbMessageIsDisplayed() {
        userMessage = UsbCameraMessage.Disconnected
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_disconnected)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun usbNotSupportedUserMessage_usbMessageIsDisplayed() {
        userMessage = UsbCameraMessage.NotSupported
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_unsupported)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun cameraRestrictionUserMessage_cameraRestrictionIsDisplayed() {
        userMessage = CameraRestrictionMessage()
        val title = composeTestRule.activity.getString(R.string.kaleyra_user_has_no_video_permissions)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun genericAudioFailureMessage_genericAudioFailureIsDisplayed() {
        userMessage = AudioConnectionFailureMessage.Generic
        val title = composeTestRule.activity.getString(R.string.kaleyra_generic_audio_routing_error)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun inSystemCallAudioFailureMessage_inSystemCallAudioFailureIsDisplayed() {
        userMessage = AudioConnectionFailureMessage.InSystemCall
        val title = composeTestRule.activity.getString(R.string.kaleyra_already_in_system_call_audio_routing_error)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }
}