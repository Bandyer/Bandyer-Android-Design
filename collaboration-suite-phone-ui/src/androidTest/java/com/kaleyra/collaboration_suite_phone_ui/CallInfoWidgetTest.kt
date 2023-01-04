package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Recording
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallInfoWidgetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callInfo by mutableStateOf(callInfoMock)

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallInfoWidget(
                callInfo = callInfo,
                onBackPressed = { isBackPressed = true }
            )
        }
    }

    @Test
    fun backButtonIsDisplayed() {
        composeTestRule.findBackButton().assertIsDisplayed()
    }

    @Test
    fun userClicksBack_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(isBackPressed)
    }

    @Test
    fun recordingFalse_recordingLabelDoesNotExists() {
        callInfo = callInfoMock.copy(recording = null)
        findRecordingLabel().assertDoesNotExist()
    }

    @Test
    fun recordingTrue_recordingLabelIsDisplayed() {
        callInfo = callInfoMock.copy(recording = Recording.MANUAL)
        findRecordingLabel().assertIsDisplayed()
    }

    @Test
    fun watermarkImageNotNull_watermarkImageIsDisplayed() {
        callInfo = callInfoMock.copy(
            watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = null)
        )
        findWatermarkImage().assertIsDisplayed()
    }

    @Test
    fun watermarkTextNotNull_watermarkTextIsDisplayed() {
        callInfo = callInfoMock.copy(watermarkInfo = WatermarkInfo(image = null, text = "watermark"))
        composeTestRule.onNodeWithText("watermark").assertIsDisplayed()
    }

    @Test
    fun watermarkInfoNotNull_titleIsDisplayedBelowWatermark() {
        callInfo = callInfoMock.copy(callState = CallState.Connecting, watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "watermark"))
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        val titleTop = composeTestRule.onNodeWithContentDescription(connecting).getBoundsInRoot().top
        val watermarkBottom = findWatermarkImage().getBoundsInRoot().bottom
        assert(titleTop > watermarkBottom)
    }

    @Test
    fun watermarkInfoNull_titleIsDisplayedToEndOfBackButton() {
        callInfo = callInfoMock.copy(callState = CallState.Connecting, watermarkInfo = null)
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        val subtitleLeft = composeTestRule.onNodeWithContentDescription(connecting).getBoundsInRoot().left
        val backRight = composeTestRule.findBackButton().getBoundsInRoot().right
        assert(subtitleLeft > backRight)
    }

    // NB: The title is actually an AndroidView, because there is not text ellipsize in compose
    // It is findable by using the content description because it is added in the AndroidView's semantics
    @Test
    fun callStateConnecting_connectingTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Connecting)
        composeTestRule.assertConnectingTitleIsDisplayed()
    }

    @Test
    fun callStateReconnecting_connectingTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Reconnecting)
        composeTestRule.assertConnectingTitleIsDisplayed()
    }

    @Test
    fun callStateDisconnected_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.AnsweredOnAnotherDevice)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateEnded_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateDeclined_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Declined)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateError_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Error)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateServerError_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Error.Server)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateUnknownError_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Error.Unknown)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateHangUp_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.HangUp)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateKicked_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Kicked(""))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateLineBusy_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.LineBusy)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateTimeout_endedTitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Timeout)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateRinging_otherParticipantsUsernamesAreDisplayed() {
        callInfo = callInfoMock.copy(
            callState = CallState.Ringing,
            otherParticipants = listOf("user1", "user2")
        )
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateDialing_otherParticipantsUsernamesAreDisplayed() {
        callInfo = callInfoMock.copy(
            callState = CallState.Dialing,
            otherParticipants = listOf("user1", "user2")
        )
        composeTestRule.onNodeWithContentDescription("user1, user2").assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingSubtitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Dialing)
        val dialing = composeTestRule.activity.getString(R.string.kaleyra_call_status_dialing)
        composeTestRule.onNodeWithText(dialing).assertIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_answeredOnAnotherDeviceSubtitleIsDisplayed() {
        callInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.AnsweredOnAnotherDevice)
        val answered = composeTestRule.activity.getString(R.string.kaleyra_call_status_answered_on_other_device)
        composeTestRule.onNodeWithText(answered).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_declinedSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val declinedQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_status_declined, 1)
        val declinedQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_status_declined, 2)
        val declineCallInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Declined)
        callInfo = declineCallInfo.copy(otherParticipants = listOf("user1"))
        composeTestRule.onNodeWithText(declinedQuantityOne).assertIsDisplayed()
        callInfo = declineCallInfo.copy(otherParticipants = listOf("user1", "user2"))
        composeTestRule.onNodeWithText(declinedQuantityOther).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_timeoutSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val timeoutQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_status_no_answer, 1)
        val timeoutQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_status_no_answer, 2)
        val timeoutCallInfo = callInfoMock.copy(callState = CallState.Disconnected.Ended.Timeout)
        callInfo = timeoutCallInfo.copy(otherParticipants = listOf("user1"))
        composeTestRule.onNodeWithText(timeoutQuantityOne).assertIsDisplayed()
        callInfo = timeoutCallInfo.copy(otherParticipants = listOf("user1", "user2"))
        composeTestRule.onNodeWithText(timeoutQuantityOther).assertIsDisplayed()
    }

    @Test
    fun callStateRinging_ringingSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val ringingQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_status_ringing, 1)
        val ringingQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_status_ringing, 2)
        val ringingCallInfo = callInfoMock.copy(callState = CallState.Ringing)
        callInfo = ringingCallInfo.copy(otherParticipants = listOf("user1"))
        composeTestRule.onNodeWithText(ringingQuantityOne).assertIsDisplayed()
        callInfo = ringingCallInfo.copy(otherParticipants = listOf("user1", "user2"))
        composeTestRule.onNodeWithText(ringingQuantityOther).assertIsDisplayed()
    }

    private fun findRecordingLabel(): SemanticsNodeInteraction {
        val rec = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec).uppercase()
        return composeTestRule.onNodeWithText(rec)
    }

    private fun findWatermarkImage(): SemanticsNodeInteraction {
        val logo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        return composeTestRule.onNodeWithContentDescription(logo)
    }

    private fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.assertConnectingTitleIsDisplayed() {
        val connecting = activity.getString(R.string.kaleyra_call_status_connecting)
         onNodeWithContentDescription(connecting).assertIsDisplayed()
    }

    private fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.assertEndedTitleIsDisplayed() {
        val connecting = activity.getString(R.string.kaleyra_call_status_ended)
        onNodeWithContentDescription(connecting).assertIsDisplayed()
    }

}