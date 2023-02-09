package com.kaleyra.collaboration_suite_phone_ui

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallComponentTest {

    // TODO test to check title, subtitle, watermark and recording and when to show call info widget
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val featuredStreamsMock = ImmutableList(listOf(streamUiMock.copy(username = "user1"), streamUiMock.copy(username = "user2")))

    private var state by mutableStateOf(defaultState())

    private var isBackPressed = false

    @Before
    fun setUp() {
        state = defaultState()
        composeTestRule.setContent {
            CallComponent(
                state = state,
                onBackPressed = { isBackPressed = true }
            )
        }
        isBackPressed = false
    }

    @Test
    fun userClicksCallInfoWidgetBackButton_onBackPressedInvoked() {
        state = defaultState(CallUiState(callState = CallStateUi.Connecting))
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksStreamBackButton_onBackPressedInvoked() {
        state = defaultState(CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock))
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksEnterFullscreen_fullscreenStreamIsDisplayed() {
        state = defaultState(CallUiState(featuredStreams = featuredStreamsMock))
        composeTestRule.onAllNodesWithContentDescription(getEnterFullscreenText()).onFirst().performClick()
        composeTestRule.fullscreenStreamIsDisplayed("user1")
    }

    @Test
    fun userClicksExitFullscreen_streamsGridIsDisplayed() {
        state = defaultState(
            callUiState = CallUiState(featuredStreams = featuredStreamsMock),
            fullscreenStream = streamUiMock.copy(username = "user1")
        )
        composeTestRule.onNodeWithContentDescription(getExitFullscreenText()).performClick()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun fullscreenStream_userClicksCallInfoWidgetBackButton_streamsGridIsDisplayed() {
        state = defaultState(
            callUiState = CallUiState(callState = CallStateUi.Connecting, featuredStreams = featuredStreamsMock),
            fullscreenStream = streamUiMock.copy(username = "user1")
        )
        composeTestRule.onAllNodesWithContentDescription(getExitFullscreenText()).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun fullscreenStream_userClicksStreamBackButton_streamsGridIsDisplayed() {
        state = defaultState(
            callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock),
            fullscreenStream = streamUiMock.copy(username = "user1")
        )
        composeTestRule.onAllNodesWithContentDescription(getExitFullscreenText()).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun enterFullscreenMode_fullscreenStreamIsDisplayed() {
        state = defaultState(CallUiState(featuredStreams = featuredStreamsMock))
        composeTestRule.streamGridIsDisplayed("user1", "user2")
        state.enterFullscreenMode(streamUiMock.copy(username = "user1"))
        composeTestRule.fullscreenStreamIsDisplayed("user1")
    }

    @Test
    fun exitFullscreenMode_streamsGridIsDisplayed() {
        state = defaultState(
            callUiState = CallUiState(featuredStreams = featuredStreamsMock),
            fullscreenStream = streamUiMock.copy(username = "user1")
        )
        composeTestRule.onNodeWithText("user1").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription(getExitFullscreenText()).assertCountEquals(1)
        state.exitFullscreenMode()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun enterFullscreenMode_stateFullscreenStreamIsTheCorrect() {
        val stream = streamUiMock.copy(username = "user1")
        assertEquals(null, state.fullscreenStream)
        state.enterFullscreenMode(stream)
        assertEquals(stream, state.fullscreenStream)
    }

    @Test
    fun exitFullscreenMode_stateFullscreenStreamIsNull() {
        val stream = streamUiMock.copy(username = "user1")
        state = defaultState(fullscreenStream = stream)
        assertEquals(stream, state.fullscreenStream)
        state.exitFullscreenMode()
        assertEquals(null, state.fullscreenStream)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsLessThan600Dp_oneColumn() {
       state = defaultState(
           configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
           maxWidth = 599.dp
       )
        assertEquals(1, state.columns)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsMoreThan600DpAndStreamsCountIsGreaterThanTwo_twoColumns() {
        state = defaultState(
            callUiState = CallUiState(featuredStreams = featuredStreamsMock),
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 600.dp
        )
        assertEquals(2, state.columns)
    }

    @Test
    fun deviceIsNotInPortraitAndStreamsCountIsGreaterThanOne_twoColumns() {
        state = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_LANDSCAPE },
            callUiState = CallUiState(featuredStreams = ImmutableList(listOf(streamUiMock.copy(username = "user1"), streamUiMock.copy(username = "user2"), streamUiMock.copy(username = "user3"))))
        )
        assertEquals(2, state.columns)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsLessThan600Dp_columnsNumberIsOne() {
        state = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 599.dp
        )
        assertEquals(1, state.columns)
    }

    @Test
    fun gridHasOneColumnAndCallInfoWidgetIsDisplayed_firstStreamHeaderIsShifted() {
        val configurationMock = mockk<Configuration> { orientation = Configuration.ORIENTATION_PORTRAIT }
        val maxWidth = 400.dp
        state = defaultState(
            callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock),
            configuration = configurationMock,
            maxWidth = maxWidth
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val streamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        state = defaultState(
            callUiState = CallUiState(callState = CallStateUi.Connecting, featuredStreams = featuredStreamsMock),
            configuration = configurationMock,
            maxWidth = maxWidth
        )
        val newStreamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val newStreamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        val callInfoWidgetHeight = composeTestRule.onNodeWithTag(CallInfoWidgetTag).getBoundsInRoot().height
        assertEquals(newStreamOneTextTop, streamOneTextTop + callInfoWidgetHeight)
        assertEquals(streamTwoTextTop, newStreamTwoTextTop)
    }

    @Test
    fun gridHasTwoColumnsAndCallInfoWidgetIsDisplayed_bothStreamsHeaderAreShifted() {
        val configurationMock = mockk<Configuration> { orientation = Configuration.ORIENTATION_PORTRAIT }
        val maxWidth = 800.dp
        state = defaultState(
            callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock),
            configuration = configurationMock,
            maxWidth = maxWidth
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val streamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        state = defaultState(
            callUiState = CallUiState(callState = CallStateUi.Connecting, featuredStreams = featuredStreamsMock),
            configuration = configurationMock,
            maxWidth = maxWidth
        )
        val newStreamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val newStreamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        val callInfoWidgetHeight = composeTestRule.onNodeWithTag(CallInfoWidgetTag).getBoundsInRoot().height
        assertEquals(newStreamOneTextTop, streamOneTextTop + callInfoWidgetHeight)
        assertEquals(newStreamTwoTextTop, streamTwoTextTop + callInfoWidgetHeight)
    }

    @Test
    fun fullscreenStreamAndCallInfoWidgetIsDisplayed_streamHeaderIsShifted() {
        state = defaultState(
            callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock),
            fullscreenStream = streamUiMock.copy(username = "user1")
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        state = defaultState(
            callUiState = CallUiState(
                callState = CallStateUi.Connecting,
                featuredStreams = featuredStreamsMock
            ),
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 800.dp
        )
        val newStreamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val callInfoWidgetHeight = composeTestRule.onNodeWithTag(CallInfoWidgetTag).getBoundsInRoot().height
        assertEquals(newStreamOneTextTop, streamOneTextTop + callInfoWidgetHeight)
    }

    @Test
    fun fullscreenStreamIsRemovedFromStreamsList_streamsGridIsDisplayed() {
        val stream = streamUiMock.copy(username = "user1")
        state = defaultState(
            callUiState = CallUiState(featuredStreams = ImmutableList(listOf(stream, streamUiMock.copy(username = "user2")))),
            fullscreenStream = stream
        )
        composeTestRule.fullscreenStreamIsDisplayed("user1")
        state = defaultState(
            callUiState = CallUiState(featuredStreams = ImmutableList(listOf(streamUiMock.copy(username = "user2"), streamUiMock.copy(username = "user3")))),
            fullscreenStream = stream
        )
        composeTestRule.streamGridIsDisplayed("user2", "user3")
    }

    @Test
    fun watermarkInfoNotNull_titleIsDisplayedBelowWatermark() {
        state = defaultState(
            callUiState = CallUiState(
                callState = CallStateUi.Connecting,
                watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "watermark")
            )
        )
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        val titleTop = composeTestRule.onNodeWithContentDescription(connecting).getBoundsInRoot().top

        val logo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        val watermarkBottom = composeTestRule.onNodeWithContentDescription(logo).getBoundsInRoot().bottom
        assert(titleTop > watermarkBottom)
    }

    @Test
    fun watermarkInfoNull_titleIsDisplayedToEndOfBackButton() {
        state = defaultState(CallUiState(callState = CallStateUi.Connecting, watermarkInfo = null))
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        val subtitleLeft = composeTestRule.onNodeWithContentDescription(connecting).getBoundsInRoot().left
        val backRight = composeTestRule.findBackButton().getBoundsInRoot().right
        assert(subtitleLeft > backRight)
    }

    // NB: The title is actually an AndroidView, because there is not text ellipsize in compose
    // It is findable by using the content description because it is added in the AndroidView's semantics
    @Test
    fun callStateConnecting_connectingTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Connecting))
        composeTestRule.assertConnectingTitleIsDisplayed()
    }

    @Test
    fun callStateReconnecting_connectingTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Reconnecting))
        composeTestRule.assertConnectingTitleIsDisplayed()
    }

    @Test
    fun callStateDisconnected_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateEnded_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateDeclined_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Declined))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateError_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Error))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateServerError_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Error.Server))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateUnknownError_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Error.Unknown))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateHangUp_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.HungUp))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateKicked_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Kicked("")))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateLineBusy_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.LineBusy))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateTimeout_endedTitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Timeout))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_answeredOnAnotherDeviceSubtitleIsDisplayed() {
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice))
        val answered = composeTestRule.activity.getString(R.string.kaleyra_call_status_answered_on_other_device)
        composeTestRule.onNodeWithText(answered).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_declinedSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val declinedQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_status_declined, 1)
        val declinedQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_status_declined, 2)
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Declined, isGroupCall = false))
        composeTestRule.onNodeWithText(declinedQuantityOne).assertIsDisplayed()
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Declined, isGroupCall = true))
        composeTestRule.onNodeWithText(declinedQuantityOther).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_timeoutSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val timeoutQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_status_no_answer, 1)
        val timeoutQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_status_no_answer, 2)
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Timeout, isGroupCall = false))
        composeTestRule.onNodeWithText(timeoutQuantityOne).assertIsDisplayed()
        state = defaultState(CallUiState(callState = CallStateUi.Disconnected.Ended.Timeout, isGroupCall = true))
        composeTestRule.onNodeWithText(timeoutQuantityOther).assertIsDisplayed()
    }

    private fun ComposeContentTestRule.streamGridIsDisplayed(vararg usernames: String) {
        onAllNodesWithContentDescription(getEnterFullscreenText()).assertCountEquals(usernames.size)
        usernames.forEach { onNodeWithText(it).assertIsDisplayed() }
    }

    private fun ComposeContentTestRule.fullscreenStreamIsDisplayed(username: String) {
        val exitFullscreen = getExitFullscreenText()
        onNodeWithText(username).assertIsDisplayed()
        onAllNodesWithContentDescription(exitFullscreen).assertCountEquals(1)
        onNodeWithContentDescription(exitFullscreen).assertIsDisplayed()
    }

    private fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.assertConnectingTitleIsDisplayed() {
        val connecting = activity.getString(R.string.kaleyra_call_status_connecting)
        onNodeWithContentDescription(connecting).assertIsDisplayed()
    }

    private fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.assertEndedTitleIsDisplayed() {
        val connecting = activity.getString(R.string.kaleyra_call_status_ended)
        onNodeWithContentDescription(connecting).assertIsDisplayed()
    }

    private fun getBackText() = composeTestRule.activity.getString(R.string.kaleyra_back)

    private fun getEnterFullscreenText() = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)

    private fun getExitFullscreenText() = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)

    private fun defaultState(
        callUiState: CallUiState = CallUiState(),
        configuration: Configuration = mockk(),
        maxWidth: Dp = 400.dp,
        fullscreenStream: StreamUi? = null
    ): CallComponentState {
        return CallComponentState(
            callUiState = callUiState,
            configuration = configuration,
            maxWidth = maxWidth,
            fullscreenStream = fullscreenStream
        )
    }
}