package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActionsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<CallAction>()))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallActions(items = items, 4)
        }
    }

    @Test
    fun cameraAction_cameraItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Camera(isToggled = false, isEnabled = false) { }))
        val disableVideo = composeTestRule.activity.getString(R.string.kaleyra_call_action_video_disable)
        val disableVideoDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_camera_description)
        composeTestRule.onNodeWithText(disableVideo).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(disableVideoDesc).assertIsDisplayed()
    }

    @Test
    fun microphoneAction_micItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Microphone(isToggled = false, isEnabled = false) { }))
        val muteMic = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute)
        val muteMicDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_mic_description)
        composeTestRule.onNodeWithText(muteMic).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(muteMicDesc).assertIsDisplayed()
    }

    @Test
    fun switchCameraAction_switchCameraItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.SwitchCamera(isEnabled = false) { }))
        val switchCamera = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera)
        val switchCameraDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera_description)
        composeTestRule.onNodeWithText(switchCamera).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(switchCameraDesc).assertIsDisplayed()
    }

    @Test
    fun hangUpAction_hangUpItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.HangUp(isEnabled = false) { }))
        val hangUp = composeTestRule.activity.getString(R.string.kaleyra_call_hangup)
        composeTestRule.onNodeWithText(hangUp).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(hangUp).assertIsDisplayed()
    }

    @Test
    fun chatAction_chatItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Chat(isEnabled = false) { }))
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_action_chat)
        composeTestRule.onNodeWithText(chat).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(chat).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Whiteboard(isEnabled = false) { }))
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(whiteboard).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.FileShare(isEnabled = false) { }))
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(fileShare).assertIsDisplayed()
    }

    @Test
    fun audioAction_audioItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Audio(isEnabled = false) { }))
        val audioRoute = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithText(audioRoute).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(audioRoute).assertIsDisplayed()
    }

    @Test
    fun screenShareAction_screenShareItemIsDisplayed() {
        items = ImmutableList(listOf(CallAction.ScreenShare(isEnabled = false) { }))
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        composeTestRule.onNodeWithText(screenShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(screenShare).assertIsDisplayed()
    }
}