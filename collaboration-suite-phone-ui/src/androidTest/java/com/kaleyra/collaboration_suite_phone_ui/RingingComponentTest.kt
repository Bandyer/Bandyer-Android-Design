package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Recording
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.ringing.RingingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RingingComponentTest: PreCallComponentTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    override var uiState = mutableStateOf(PreCallUiState(stream = streamUiMock))

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
            recordingValue = Recording.OnConnect,
            expectedText = automatic
        )
    }

    @Test
    fun recordingAutomatic_automaticRecordingTextIsDisplayed() {
        val manual =
            composeTestRule.activity.getString(R.string.kaleyra_manual_recording_disclaimer)
        composeTestRule.assertRecordingTextIsDisplayed(
            recordingValue = Recording.OnDemand,
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

    private fun ComposeTestRule.assertRingingButtonIsDisplayed(text: String) {
        onAllNodesWithContentDescription(text).onFirst().assertHasClickAction()
        onAllNodesWithContentDescription(text).onFirst().assertIsDisplayed()
        onNodeWithText(text).assertIsDisplayed()
    }

    private fun ComposeTestRule.assertRecordingTextIsDisplayed(
        recordingValue: Recording,
        expectedText: String
    ) {
        uiState.value = PreCallUiState(recording = null)
        onNodeWithText(expectedText).assertDoesNotExist()
        uiState.value = PreCallUiState(recording = recordingValue)
        onNodeWithText(expectedText).assertIsDisplayed()
    }

}