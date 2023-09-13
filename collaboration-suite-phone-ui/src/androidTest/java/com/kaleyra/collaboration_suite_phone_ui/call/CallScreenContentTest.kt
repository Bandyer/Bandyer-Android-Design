package com.kaleyra.collaboration_suite_phone_ui.call

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.*
import com.kaleyra.collaboration_suite_phone_ui.call.dialing.DialingContentTag
import com.kaleyra.collaboration_suite_phone_ui.call.ringing.RingingContentTag
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.screen.view.CallComponentTag
import com.kaleyra.collaboration_suite_phone_ui.call.screen.view.CallScreenContent
import com.kaleyra.collaboration_suite_phone_ui.call.screen.viewmodel.CallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.callinfowidget.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.findBackButton
import io.mockk.every
import io.mockk.mockkConstructor
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callState by mutableStateOf<CallStateUi>(CallStateUi.Disconnected)

    private var backPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallScreenContent(
                callState = callState,
                maxWidth = 600.dp,
                onBackPressed = { backPressed = true },
                onStreamFullscreenClick = {}
            )
        }
    }

    @After
    fun tearDown() {
        backPressed = false
    }

    @Test
    fun callStateRinging_ringingContentIsDisplayed() {
        callState = CallStateUi.Ringing()
        composeTestRule.onNodeWithTag(RingingContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_dialingContentIsDisplayed() {
        callState = CallStateUi.Dialing
        composeTestRule.onNodeWithTag(DialingContentTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnected_callContentIsDisplayed() {
        callState = CallStateUi.Connected
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_callContentIsDisplayed() {
        callState = CallStateUi.Reconnecting
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDisconnected_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateEnded_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateHungUp_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.HungUp
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Declined
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateKicked_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Kicked("")
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateLineBusy_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.LineBusy
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Timeout
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateError_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Error
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateServerError_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Error.Server
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateUnknownError_callContentIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Error.Unknown
        composeTestRule.onNodeWithTag(CallComponentTag).assertIsDisplayed()
    }

    @Test
    fun callStateRinging_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Ringing()
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDialing_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Dialing
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Reconnecting
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDisconnected_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateEnded_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateHungUp_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.HungUp
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Declined
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateKicked_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Kicked("")
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateLineBusy_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.LineBusy
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Timeout
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateError_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Error
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateServerError_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Error.Server
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateUnknownError_callInfoWidgetIsDisplayed() {
        callState = CallStateUi.Disconnected.Ended.Error.Unknown
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnected_userClicksStreamBackButton_onBackPressedInvoked() {
        mockkConstructor(CallViewModel::class)
        every { anyConstructed<CallViewModel>().uiState } returns MutableStateFlow(CallUiState(featuredStreams = ImmutableList(listOf(streamUiMock))))
        callState = CallStateUi.Connected
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateDialing_userClicksInfoWidgetBackButton_onBackPressedInvoked() {
        callState = CallStateUi.Dialing
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun callStateRinging_userClicksInfoWidgetBackButton_onBackPressedInvoked() {
        callState = CallStateUi.Ringing()
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }
}