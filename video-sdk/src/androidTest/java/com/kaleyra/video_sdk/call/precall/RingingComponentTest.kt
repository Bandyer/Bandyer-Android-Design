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

package com.kaleyra.video_sdk.call.precall

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.dialing.view.DialingUiState
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.recording.model.RecordingTypeUi
import com.kaleyra.video_sdk.call.ringing.RingingComponent
import com.kaleyra.video_sdk.call.ringing.model.RingingUiState
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.core.StreamOverlayTestTag
import com.kaleyra.video_sdk.call.stream.view.core.StreamViewTestTag
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.findAvatar
import com.kaleyra.video_sdk.findBackButton
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RingingComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val initialState = RingingUiState(video = streamUiMock.video, participants = ImmutableList(listOf("user1", "user2")))
    
    private var uiState by mutableStateOf(initialState)

    private var userMessage by mutableStateOf<UserMessage?>(null)

    private var timerMillis by mutableStateOf(0L)

    private var backPressed = false

    private var answerClicked = false

    private var declineClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            RingingComponent(
                uiState = uiState,
                userMessage = userMessage,
                tapToAnswerTimerMillis = timerMillis,
                onBackPressed = { backPressed = true },
                onAnswerClick = { answerClicked = true },
                onDeclineClick = { declineClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        uiState = initialState
        timerMillis = 0L
        backPressed = false
        answerClicked = false
        declineClicked = false
    }

    @Test
    fun videoNull_avatarDisplayed() {
        uiState = uiState.copy(video = null)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNull_avatarDisplayed() {
        val video = VideoUi(id = "videoId", view = null, isEnabled = false)
        uiState = uiState.copy(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndDisabled_avatarIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false)
        uiState = uiState.copy(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewNotNullAndEnabled_streamIsDisplayed() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
        uiState = uiState.copy(video = video)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun participantListIsEmpty_avatarIsNotDisplay() {
        val video = VideoUi(id = "videoId", view = null, isEnabled = false)
        uiState = uiState.copy(video = video, participants = ImmutableList())
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun isLinkTrue_connectingIsDisplayed() {
        uiState = uiState.copy(isLink = true)
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        composeTestRule.onNodeWithText(connecting).assertIsDisplayed()
    }

    @Test
    fun isConnectingTrue_connectingIsDisplayed() {
        uiState = uiState.copy(isConnecting = true)
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        composeTestRule.onNodeWithText(connecting).assertIsDisplayed()
    }

    @Test
    fun answerButtonIsDisplayed() {
        val answer = composeTestRule.activity.getString(R.string.kaleyra_ringing_answer)
        composeTestRule.assertRingingButtonIsDisplayed(answer)
    }

    @Test
    fun declineButtonIsDisplayed() {
        val decline = composeTestRule.activity.getString(R.string.kaleyra_ringing_decline)
        composeTestRule.assertRingingButtonIsDisplayed(decline)
    }

    @Test
    fun recordingManual_manualRecordingTextIsDisplayed() {
        val automatic =
            composeTestRule.activity.getString(R.string.kaleyra_automatic_recording_disclaimer)
        composeTestRule.assertRecordingTextIsDisplayed(
            recordingValue = RecordingTypeUi.OnConnect,
            expectedText = automatic
        )
    }

    @Test
    fun recordingAutomatic_automaticRecordingTextIsDisplayed() {
        val manual =
            composeTestRule.activity.getString(R.string.kaleyra_manual_recording_disclaimer)
        composeTestRule.assertRecordingTextIsDisplayed(
            recordingValue = RecordingTypeUi.OnDemand,
            expectedText = manual
        )
    }

    @Test
    fun timerMillisIsZero_tapToAnswerIsDisplayed() {
        val tapToAnswer = composeTestRule.activity.getString(R.string.kaleyra_tap_to_answer)
        timerMillis = 0L
        composeTestRule.onNodeWithText(tapToAnswer).assertIsDisplayed()
    }

    @Test
    fun usersClicksBackButton_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun usersClicksAnswerButton_onAnswerClickInvoked() {
        val answer = composeTestRule.activity.getString(R.string.kaleyra_ringing_answer)
        composeTestRule.onAllNodesWithContentDescription(answer).onFirst().performClick()
        assert(answerClicked)
    }

    @Test
    fun usersClicksDeclineButton_onDeclineClickInvoked() {
        val decline = composeTestRule.activity.getString(R.string.kaleyra_ringing_decline)
        composeTestRule.onAllNodesWithContentDescription(decline).onFirst().performClick()
        assert(declineClicked)
    }


    @Test
    fun callStateRinging_otherParticipantsUsernamesAreDisplayed() {
        uiState = uiState.copy(participants = ImmutableList(listOf("user1", "user2")))
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateRinging_ringingSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val ringingQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_incoming_status_ringing, 1)
        val ringingQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_incoming_status_ringing, 2)
        uiState = uiState.copy(participants = ImmutableList(listOf("user1")))
        composeTestRule.onNodeWithText(ringingQuantityOne).assertIsDisplayed()
        uiState = uiState.copy(participants = ImmutableList(listOf("user1", "user2")))
        composeTestRule.onNodeWithText(ringingQuantityOther).assertIsDisplayed()
    }

    @Test
    fun amIWaitingOthersTrue_waitingForOtherParticipantsIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_waiting_for_other_participants)
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        uiState = uiState.copy(amIWaitingOthers = true)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun isVideoIncomingTrueAndVideoIsNull_avatarIsNotDisplayed() {
        uiState = uiState.copy(isVideoIncoming = true, video = null)
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun isVideoIncomingFalseAndVideoIsNull_avatarIsDisplayed() {
        uiState = uiState.copy(isVideoIncoming = false, video = null)
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoViewIsNullAndVideoIsDisabled_avatarIsDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = null, isEnabled = false))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoNotNullAndVideoViewIsNullAndVideoIsEnabled_avatarIsNotDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun videoViewIsNotNullAndVideoIsDisabled_avatarIsDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = false))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun videoIsEnabled_avatarIsNotDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun streamViewNotNullAndVideoEnabled_overlayIsDisplayed() {
        uiState = uiState.copy(video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.onNodeWithTag(StreamOverlayTestTag).assertIsDisplayed()
    }

    @Test
    fun userMessage_userMessageSnackbarIsDisplayed() {
        userMessage = RecordingMessage.Started
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    private fun ComposeTestRule.assertRingingButtonIsDisplayed(text: String) {
        onAllNodesWithContentDescription(text).onFirst().assertHasClickAction()
        onAllNodesWithContentDescription(text).onFirst().assertIsDisplayed()
        onNodeWithText(text).assertIsDisplayed()
    }

    private fun ComposeTestRule.assertRecordingTextIsDisplayed(
        recordingValue: RecordingTypeUi,
        expectedText: String
    ) {
        uiState = uiState.copy(recording = null)
        onNodeWithText(expectedText).assertDoesNotExist()
        uiState = uiState.copy(recording = recordingValue)
        onNodeWithText(expectedText).assertIsDisplayed()
    }

}