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

package com.kaleyra.video_sdk.ui.call.callscreen

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.screen.PipScreen
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.recording.view.RecordingDotTestTag
import com.kaleyra.video_sdk.call.stream.view.core.StreamViewTestTag
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PipScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var stream by mutableStateOf<StreamUi?>(null)

    private var callState by mutableStateOf<CallStateUi>(CallStateUi.Reconnecting)

    private var isRecording by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PipScreen(
                stream = stream,
                callState = callState,
                isGroupCall = false,
                isRecording = isRecording
            )
        }
    }

    @After
    fun tearDown() {
        stream = null
        callState = CallStateUi.Connected
        isRecording = false
    }

    @Test
    fun streamIsDisplayed() {
        stream = StreamUi(
            id = "streamId",
            username = "username",
            video = VideoUi(
                id = "videoId",
                view = ImmutableView(View(composeTestRule.activity)),
                isEnabled = true
            )
        )
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun isRecordingTrue_isRecordingLabelIsDisplayed() {
        isRecording = true
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec)
        composeTestRule.onNodeWithTag(RecordingDotTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(text, ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingTextIsDisplayed() {
        callState = CallStateUi.Dialing
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_status_ringing)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun callStateRinging_ringingTextIsDisplayed() {
        callState = CallStateUi.Ringing()
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_incoming_status_ringing, 1)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_reconnectingTextIsDisplayed() {
        callState = CallStateUi.Reconnecting
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_offline)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }
}