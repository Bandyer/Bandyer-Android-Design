package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streams by mutableStateOf(ImmutableList(listOf(streamUiMock)))

    private var callInfo by mutableStateOf(callInfoMock)

    private var backPressed = false

    private var answerClicked = false

    private var declineClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallScreenContent(
                streams = streams,
                callInfo = callInfo,
                onBackPressed = { backPressed = true },
                onAnswerClick = { answerClicked = true },
                onDeclineClick = { declineClicked = true }
            )
        }
        backPressed = false
        answerClicked = false
        declineClicked = false
    }

    @Test
    fun callStateRinging_ringingContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        composeTestRule.onNodeWithTag(RingingContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Dialing)
        composeTestRule.onNodeWithTag(DialingContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnecting_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Connecting)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnected_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Connected)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Reconnecting)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDisconnected_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateEnded_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateHangUp_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.HangUp)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Declined)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateKicked_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Kicked(""))
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.AnsweredOnAnotherDevice)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateLineBusy_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.LineBusy)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Timeout)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateError_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Error)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateServerError_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Error.Server)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateUnknownError_callContentIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Error.Unknown)
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateRinging_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Dialing)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Reconnecting)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnecting_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Connecting)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDisconnected_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateEnded_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateHangUp_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.HangUp)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Declined)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateKicked_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Kicked(""))
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.AnsweredOnAnotherDevice)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateLineBusy_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.LineBusy)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Timeout)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateError_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Error)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateServerError_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Error.Server)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateUnknownError_callInfoWidgetIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Disconnected.Ended.Error.Unknown)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnected_userClicksBackButton_onBackPressedInvoked() {
        callInfo = callInfo.copy(callState = CallState.Connected)
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateConnecting_userClicksBackButton_onBackPressedInvoked() {
        callInfo = callInfo.copy(callState = CallState.Connecting)
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateDialing_userClicksBackButton_onBackPressedInvoked() {
        callInfo = callInfo.copy(callState = CallState.Dialing)
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateRinging_userClicksBackButton_onBackPressedInvoked() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateRinging_userClicksAnswerButton_onAnswerClickInvoked() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        val answer = composeTestRule.activity.getString(R.string.kaleyra_ringing_answer)
        composeTestRule.onAllNodesWithContentDescription(answer).onFirst().performClick()
        assert(answerClicked)
    }

    @Test
    fun callStateRinging_userClicksDeclineButton_onDeclineClickInvoked() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        val decline = composeTestRule.activity.getString(R.string.kaleyra_ringing_decline)
        composeTestRule.onAllNodesWithContentDescription(decline).onFirst().performClick()
        assert(declineClicked)
    }

    @Test
    fun callStateRinging_emptyStreamList_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        streams = ImmutableList(listOf())
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateRinging_streamViewNull_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        streams = ImmutableList(listOf(streamUiMock.copy(view = null)))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateRinging_streamHasVideoDisabled_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        streams = ImmutableList(listOf(streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = false)))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateRinging_streamHasVideoEnabled_streamIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Ringing)
        streams = ImmutableList(listOf(streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = true)))
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_emptyStreamList_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Dialing)
        streams = ImmutableList(listOf())
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateDialing_streamViewNull_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Dialing)
        streams = ImmutableList(listOf(streamUiMock.copy(view = null)))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateDialing_streamHasVideoDisabled_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Dialing)
        streams = ImmutableList(listOf(streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = false)))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateDialing_streamHasVideoEnabled_streamIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Dialing)
        streams = ImmutableList(listOf(streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = true)))
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnected_streamViewNull_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Connected)
        streams = ImmutableList(listOf(streamUiMock.copy(view = null)))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateConnected_streamHasVideoDisabled_avatarIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Connected)
        streams = ImmutableList(listOf(streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = false)))
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun callStateConnected_streamHasVideoEnabled_streamIsDisplayed() {
        callInfo = callInfo.copy(callState = CallState.Connected)
        streams = ImmutableList(listOf(streamUiMock.copy(view = View(composeTestRule.activity), isVideoEnabled = true)))
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }
}