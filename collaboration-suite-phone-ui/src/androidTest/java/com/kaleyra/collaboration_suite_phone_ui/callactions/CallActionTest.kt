package com.kaleyra.collaboration_suite_phone_ui.callactions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.view.CallAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var action by mutableStateOf<CallAction>(CallAction.Audio())

    private var toggled by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallAction(
                action = action,
                onToggle = { toggled = it },
            )
        }
        toggled = false
    }

    @Test
    fun buttonIsToggleable() {
        composeTestRule.onRoot().onChildAt(0).assertIsToggleable()
    }

    @Test
    fun cameraAction_cameraActionTextAndIconDisplayed() {
        action = CallAction.Camera()
        val disableVideo = composeTestRule.activity.getString(R.string.kaleyra_call_action_video_disable)
        val disableVideoDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_camera_description)
        composeTestRule.onNodeWithText(disableVideo).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(disableVideoDesc).assertIsDisplayed()
    }

    @Test
    fun microphoneAction_micActionTextAndIconDisplayed() {
        action = CallAction.Microphone()
        val muteMic = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute)
        val muteMicDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_mic_description)
        composeTestRule.onNodeWithText(muteMic).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(muteMicDesc).assertIsDisplayed()
    }

    @Test
    fun switchCameraAction_switchCameraActionTextAndIconDisplayed() {
        action = CallAction.SwitchCamera()
        val switchCamera = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera)
        val switchCameraDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera_description)
        composeTestRule.onNodeWithText(switchCamera).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(switchCameraDesc).assertIsDisplayed()
    }

    @Test
    fun hangUpAction_hangUpActionTextAndIconDisplayed() {
        action = CallAction.HangUp()
        val hangUp = composeTestRule.activity.getString(R.string.kaleyra_call_hangup)
        composeTestRule.onNodeWithText(hangUp).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(hangUp).assertIsDisplayed()
    }

    @Test
    fun chatAction_chatItemActionTextAndIconDisplayed() {
        action = CallAction.Chat()
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_action_chat)
        composeTestRule.onNodeWithText(chat).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(chat).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardActionTextAndIconDisplayed() {
        action = CallAction.Whiteboard()
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(whiteboard).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareActionTextAndIconDisplayed() {
        action = CallAction.FileShare()
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(fileShare).assertIsDisplayed()
    }

    @Test
    fun audioAction_audioActionTextAndIconDisplayed() {
        action = CallAction.Audio()
        val audioRoute = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithText(audioRoute).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(audioRoute).assertIsDisplayed()
    }

    @Test
    fun screenShareAction_screenShareActionTextAndIconDisplayed() {
        action = CallAction.ScreenShare()
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        composeTestRule.onNodeWithText(screenShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(screenShare).assertIsDisplayed()
    }

    @Test
    fun userPerformsClick_onToggledInvoked() {
        composeTestRule.onRoot().performClick()
        assert(toggled)
    }

    @Test
    fun toggleableAction_userPerformsClick_actionIsToggled() {
        action = CallAction.Microphone(isToggled = false)
        composeTestRule.onRoot().onChildAt(0).assertIsOff()
        composeTestRule.onRoot().onChildAt(0).performClick()
        composeTestRule.onRoot().onChildAt(0).assertIsOn()
    }

    @Test
    fun isEnabledFalse_actionDisabled() {
        action = CallAction.Microphone(isEnabled = true)
        composeTestRule.onRoot().onChildAt(0).assertIsEnabled()
        action = CallAction.Microphone(isEnabled = false)
        composeTestRule.onRoot().onChildAt(0).assertIsNotEnabled()
    }
}