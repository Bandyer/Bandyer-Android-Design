package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.ringing.RingingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RingingComponentTest: PreCallComponentTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    override var uiState = mutableStateOf(PreCallUiState(video = streamUiMock.video))

    private var timerMillis by mutableStateOf(0L)

    private var backPressed = false

    private var answerClicked = false

    private var declineClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            RingingComponent(
                uiState = uiState.value,
                tapToAnswerTimerMillis = timerMillis,
                onBackPressed = { backPressed = true },
                onAnswerClick = { answerClicked = true },
                onDeclineClick = { declineClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        uiState.value = PreCallUiState(video = streamUiMock.video)
        timerMillis = 0L
        backPressed = false
        answerClicked = false
        declineClicked = false
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
        uiState.value = PreCallUiState(participants = ImmutableList(listOf("user1", "user2")))
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateRinging_ringingSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val ringingQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_incoming_status_ringing, 1)
        val ringingQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_incoming_status_ringing, 2)
        uiState.value = PreCallUiState(participants = ImmutableList(listOf("user1")))
        composeTestRule.onNodeWithText(ringingQuantityOne).assertIsDisplayed()
        uiState.value = PreCallUiState(participants = ImmutableList(listOf("user1", "user2")))
        composeTestRule.onNodeWithText(ringingQuantityOther).assertIsDisplayed()
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
        uiState.value = PreCallUiState(recording = null)
        onNodeWithText(expectedText).assertDoesNotExist()
        uiState.value = PreCallUiState(recording = recordingValue)
        onNodeWithText(expectedText).assertIsDisplayed()
    }

}