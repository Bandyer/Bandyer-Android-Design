package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.dialing.DialingContentTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.ringing.RingingContentTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.every
import io.mockk.mockkConstructor
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callState by mutableStateOf<CallState>(CallState.Disconnected)

    private var backPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallScreenContent(
                callState = callState,
                maxWidth = 600.dp,
                onBackPressed = { backPressed = true }
            )
        }
        backPressed = false
    }

    @Test
    fun callStateRinging_ringingContentIsDisplayed() {
        callState = CallState.Ringing
        composeTestRule.onNodeWithTag(RingingContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingContentIsDisplayed() {
        callState = CallState.Dialing
        composeTestRule.onNodeWithTag(DialingContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnecting_callContentIsDisplayed() {
        callState = CallState.Connecting
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnected_callContentIsDisplayed() {
        callState = CallState.Connected
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_callContentIsDisplayed() {
        callState = CallState.Reconnecting
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDisconnected_callContentIsDisplayed() {
        callState = CallState.Disconnected
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateEnded_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateHungUp_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.HungUp
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.Declined
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateKicked_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.Kicked("")
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.AnsweredOnAnotherDevice
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateLineBusy_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.LineBusy
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.Timeout
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateError_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.Error
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateServerError_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.Error.Server
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateUnknownError_callContentIsDisplayed() {
        callState = CallState.Disconnected.Ended.Error.Unknown
        composeTestRule.onNodeWithTag(CallContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateRinging_callInfoWidgetIsDisplayed() {
        callState = CallState.Ringing
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_callInfoWidgetIsDisplayed() {
        callState = CallState.Dialing
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_callInfoWidgetIsDisplayed() {
        callState = CallState.Reconnecting
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnecting_callInfoWidgetIsDisplayed() {
        callState = CallState.Connecting
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDisconnected_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateEnded_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateHungUp_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.HungUp
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.Declined
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateKicked_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.Kicked("")
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.AnsweredOnAnotherDevice
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateLineBusy_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.LineBusy
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.Timeout
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateError_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.Error
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateServerError_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.Error.Server
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateUnknownError_callInfoWidgetIsDisplayed() {
        callState = CallState.Disconnected.Ended.Error.Unknown
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnected_userClicksStreamBackButton_onBackPressedInvoked() {
        mockkConstructor(CallViewModel::class)
        every { anyConstructed<CallViewModel>().uiState } returns MutableStateFlow(CallUiState(featuredStreams = ImmutableList(listOf(streamUiMock))))
        callState = CallState.Connected
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateConnecting_userClicksInfoWidgetBackButton_onBackPressedInvoked() {
        callState = CallState.Connecting
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateDialing_userClicksInfoWidgetBackButton_onBackPressedInvoked() {
        callState = CallState.Dialing
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateRinging_userClicksInfoWidgetBackButton_onBackPressedInvoked() {
        callState = CallState.Ringing
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }
}