/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.ui.call.callactions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.view.CallActionsContent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallActionsContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<CallAction>()))

    private var isActionClicked: CallAction? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallActionsContent(
                items = items,
                itemsPerRow = 4,
                onItemClick = { action ->
                    isActionClicked = action
                }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        isActionClicked = null
    }

    @Test
    fun cameraAction_cameraActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Camera(isToggled = false, isEnabled = false)))
        val disableVideo = composeTestRule.activity.getString(R.string.kaleyra_call_action_video_disable)
        val disableVideoDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_camera_description)
        composeTestRule.onNodeWithText(disableVideo).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(disableVideoDesc).assertIsDisplayed()
    }

    @Test
    fun microphoneAction_micActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Microphone(isToggled = false, isEnabled = false)))
        val muteMic = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute)
        val muteMicDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_mic_description)
        composeTestRule.onNodeWithText(muteMic).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(muteMicDesc).assertIsDisplayed()
    }

    @Test
    fun switchCameraAction_switchCameraActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.SwitchCamera(isEnabled = false)))
        val switchCamera = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera)
        val switchCameraDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera_description)
        composeTestRule.onNodeWithText(switchCamera).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(switchCameraDesc).assertIsDisplayed()
    }

    @Test
    fun hangUpAction_hangUpActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.HangUp(isEnabled = false)))
        val hangUp = composeTestRule.activity.getString(R.string.kaleyra_call_hangup)
        composeTestRule.onNodeWithText(hangUp).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(hangUp).assertIsDisplayed()
    }

    @Test
    fun chatAction_chatActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Chat(isEnabled = false)))
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_action_chat)
        composeTestRule.onNodeWithText(chat).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(chat).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Whiteboard(isEnabled = false)))
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(whiteboard).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.FileShare(isEnabled = false)))
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(fileShare).assertIsDisplayed()
    }

    @Test
    fun audioAction_audioActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.Audio(isEnabled = false)))
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithText(audioOutput).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(audioOutput).assertIsDisplayed()
    }

    @Test
    fun screenShareAction_screenShareActionIsDisplayed() {
        items = ImmutableList(listOf(CallAction.ScreenShare(isEnabled = false)))
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        composeTestRule.onNodeWithText(screenShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(screenShare).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(CallAction.ScreenShare(), CallAction.Audio()))
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithContentDescription(audioOutput).performClick()
        assertEquals(CallAction.Audio::class.java, isActionClicked!!::class.java)
    }
}