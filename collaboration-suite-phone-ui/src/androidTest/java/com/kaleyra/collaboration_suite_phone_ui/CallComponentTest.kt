package com.kaleyra.collaboration_suite_phone_ui

import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidgetTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.FeaturedStreamTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val streamMock1 = streamUiMock.copy(id = "streamId1", username = "user1")

    private val streamMock2 = streamUiMock.copy(id = "streamId2", username = "user2")

    private val featuredStreamsMock = ImmutableList(listOf(streamMock1, streamMock2))

    private var callUiState by mutableStateOf(CallUiState())

    private var callComponentState by mutableStateOf(defaultState())

    private var isBackPressed = false

    private var fullscreenStreamId: String? = ""

    private var pipStreamRect: Rect? = null

    @Before
    fun setUp() {
        callComponentState = defaultState()
        composeTestRule.setContent {
            CallComponent(
                callUiState = callUiState,
                callComponentState = callComponentState,
                onBackPressed = { isBackPressed = true },
                onStreamFullscreenClick = { fullscreenStreamId = it },
                onPipStreamPositioned = { pipStreamRect = it }
            )
        }
    }

    @After
    fun tearDown() {
        callUiState = CallUiState()
        callComponentState = defaultState()
        isBackPressed = false
        fullscreenStreamId = ""
        pipStreamRect = null
    }

    @Test
    fun userClicksCallInfoWidgetBackButton_onBackPressedInvoked() {
        callUiState = CallUiState(callState = CallStateUi.Connecting)
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksStreamBackButton_onBackPressedInvoked() {
        callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock)
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksEnterFullscreen_onFullscreenStreamClickInvoked() {
        callUiState = CallUiState(featuredStreams = featuredStreamsMock)
        composeTestRule.onAllNodesWithContentDescription(getEnterFullscreenText()).onFirst().performClick()
        assertEquals(streamMock1.id, fullscreenStreamId)
    }

    @Test
    fun userClicksExitFullscreen_onFullscreenStreamClickInvoked() {
        callUiState = CallUiState(featuredStreams = featuredStreamsMock, fullscreenStream = streamMock1)
        composeTestRule.onNodeWithContentDescription(getExitFullscreenText()).performClick()
        assertEquals(null, fullscreenStreamId)
    }

    @Test
    fun doubleTapOnStream_onFullscreenClickInvoked() {
        callUiState = CallUiState(featuredStreams = featuredStreamsMock, fullscreenStream = streamMock1)
        composeTestRule.onAllNodesWithTag(FeaturedStreamTag).onFirst().performDoubleClick()
        assertEquals(streamMock1.id, fullscreenStreamId)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsLessThan600Dp_oneColumn() {
       callComponentState = defaultState(
           configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
           maxWidth = 599.dp
       )
        assertEquals(1, callComponentState.columns)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsMoreThan600DpAndStreamsCountIsGreaterThanTwo_twoColumns() {
        callUiState = CallUiState(featuredStreams = featuredStreamsMock)
        callComponentState = defaultState(
            featuredStreamsCount = featuredStreamsMock.count(),
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 600.dp
        )
        assertEquals(2, callComponentState.columns)
    }

    @Test
    fun deviceIsNotInPortraitAndStreamsCountIsGreaterThanOne_twoColumns() {
        val featuredStreams = ImmutableList(listOf(streamUiMock.copy(username = "user1"), streamUiMock.copy(username = "user2"), streamUiMock.copy(username = "user3")))
        callUiState = CallUiState(featuredStreams = featuredStreams)
        callComponentState = defaultState(
            featuredStreamsCount = featuredStreams.count(),
            configuration = mockk { orientation = Configuration.ORIENTATION_LANDSCAPE }
        )
        assertEquals(2, callComponentState.columns)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsLessThan600Dp_columnsNumberIsOne() {
        callComponentState = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 599.dp
        )
        assertEquals(1, callComponentState.columns)
    }

    @Test
    fun gridHasOneColumnAndCallInfoWidgetIsDisplayed_firstStreamHeaderIsShifted() {
        val configurationMock = mockk<Configuration> { orientation = Configuration.ORIENTATION_PORTRAIT }
        val maxWidth = 400.dp
        callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock)
        callComponentState = defaultState(
            featuredStreamsCount = featuredStreamsMock.count(),
            configuration = configurationMock,
            maxWidth = maxWidth
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val streamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        callUiState = CallUiState(callState = CallStateUi.Connecting, featuredStreams = featuredStreamsMock)
        callComponentState = defaultState(
            featuredStreamsCount = featuredStreamsMock.count(),
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
        callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock)
        callComponentState = defaultState(
            featuredStreamsCount = featuredStreamsMock.count(),
            configuration = configurationMock,
            maxWidth = maxWidth
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val streamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        callUiState = CallUiState(callState = CallStateUi.Connecting, featuredStreams = featuredStreamsMock)
        callComponentState = defaultState(
            featuredStreamsCount = featuredStreamsMock.count(),
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
        callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock, fullscreenStream = streamMock1)
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        callUiState = CallUiState(
            callState = CallStateUi.Connecting,
            featuredStreams = featuredStreamsMock,
            fullscreenStream = streamMock1
        )
        callComponentState = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 800.dp
        )
        val newStreamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val callInfoWidgetHeight = composeTestRule.onNodeWithTag(CallInfoWidgetTag).getBoundsInRoot().height
        assertEquals(newStreamOneTextTop, streamOneTextTop + callInfoWidgetHeight)
    }

    @Test
    fun watermarkInfoNotNull_titleIsDisplayedBelowWatermark() {
        callUiState = CallUiState(
            callState = CallStateUi.Connecting,
            watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "watermark")
        )
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        val titleTop = composeTestRule.onNodeWithContentDescription(connecting).getBoundsInRoot().top

        val logo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        val watermarkBottom = composeTestRule.onNodeWithContentDescription(logo).getBoundsInRoot().bottom
        assert(titleTop > watermarkBottom)
    }

    @Test
    fun watermarkInfoNull_titleIsDisplayedToEndOfBackButton() {
        callUiState = CallUiState(callState = CallStateUi.Connecting, watermarkInfo = null)
        val connecting = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        val subtitleLeft = composeTestRule.onNodeWithContentDescription(connecting).getBoundsInRoot().left
        val backRight = composeTestRule.findBackButton().getBoundsInRoot().right
        assert(subtitleLeft > backRight)
    }

    // NB: The title is actually an AndroidView, because there is not text ellipsize in compose
    // It is findable by using the content description because it is added in the AndroidView's semantics
    @Test
    fun callStateConnecting_connectingTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Connecting)
        composeTestRule.assertConnectingTitleIsDisplayed()
    }

    @Test
    fun callStateReconnecting_connectingTitleIsDisplayed() {
        callUiState = CallUiState(CallStateUi.Reconnecting)
        composeTestRule.assertConnectingTitleIsDisplayed()
    }

    @Test
    fun callStateDisconnected_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateEnded_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateDeclined_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Declined)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateError_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Error)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateServerError_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Error.Server)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateUnknownError_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Error.Unknown)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateHangUp_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.HungUp)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateKicked_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Kicked(""))
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateLineBusy_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.LineBusy)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateTimeout_endedTitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Timeout)
        composeTestRule.assertEndedTitleIsDisplayed()
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_answeredOnAnotherDeviceSubtitleIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice)
        val answered = composeTestRule.activity.getString(R.string.kaleyra_call_status_answered_on_other_device)
        composeTestRule.onNodeWithText(answered).assertIsDisplayed()
    }

    @Test
    fun callStateDeclined_declinedSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val declinedQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_status_declined, 1)
        val declinedQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_status_declined, 2)
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Declined, isGroupCall = false)
        composeTestRule.onNodeWithText(declinedQuantityOne).assertIsDisplayed()
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Declined, isGroupCall = true)
        composeTestRule.onNodeWithText(declinedQuantityOther).assertIsDisplayed()
    }

    @Test
    fun callStateTimeout_timeoutSubtitleIsDisplayed() {
        val resources = composeTestRule.activity.resources
        val timeoutQuantityOne = resources.getQuantityString(R.plurals.kaleyra_call_status_no_answer, 1)
        val timeoutQuantityOther = resources.getQuantityString(R.plurals.kaleyra_call_status_no_answer, 2)
        callUiState = CallUiState(callState = CallStateUi.Disconnected.Ended.Timeout, isGroupCall = false)
        composeTestRule.onNodeWithText(timeoutQuantityOne).assertIsDisplayed()
        callUiState= CallUiState(callState = CallStateUi.Disconnected.Ended.Timeout, isGroupCall = true)
        composeTestRule.onNodeWithText(timeoutQuantityOther).assertIsDisplayed()
    }

    @Test
    fun callStateRecording_recordingIsDisplayed() {
        val recording = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec)
        callUiState = CallUiState(recording = RecordingUi(RecordingTypeUi.OnDemand, RecordingStateUi.Stopped))
        composeTestRule.onNodeWithText(recording, ignoreCase = true).assertDoesNotExist()
        callUiState = CallUiState(recording = RecordingUi(RecordingTypeUi.OnDemand, RecordingStateUi.Started))
        composeTestRule.onNodeWithText(recording, ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun backButtonIsDisplayedOnlyOnTheFirstStream() {
        callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = featuredStreamsMock)
        val featuredStreams = composeTestRule.onAllNodesWithTag(FeaturedStreamTag)
        val firstGridStream = featuredStreams.onFirst().getBoundsInRoot()
        val streamWithBackButton = featuredStreams.filterToOne(hasAnyChild(hasContentDescription(getBackText()))).getBoundsInRoot()
        assertEquals(streamWithBackButton, firstGridStream)
    }

    @Test
    fun callStateConnecting_callInfoWidgetIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Connecting)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateReconnecting_callInfoWidgetIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Reconnecting)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateDisconnected_callInfoWidgetIsDisplayed() {
        callUiState = CallUiState(callState = CallStateUi.Disconnected)
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callIsRecording_callInfoWidgetIsDisplayed() {
        callUiState = CallUiState(recording = RecordingUi(RecordingTypeUi.OnDemand, RecordingStateUi.Started))
        composeTestRule.onNodeWithTag(CallInfoWidgetTag).assertIsDisplayed()
    }

    @Test
    fun callStateConnectedAndRecordingTrue_watermarkIsNotDisplayed() {
        callUiState = CallUiState(
            callState = CallStateUi.Connected,
            watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "watermark")
        )
        composeTestRule.onNodeWithTag(WatermarkTag).assertDoesNotExist()
    }

    @Test
    fun onPipStreamPositionedInvoked() {
        val video = VideoUi(id = "videoId", view = ImmutableView(View(composeTestRule.activity)), isEnabled = true)
        val stream = streamMock1.copy(video = video)
        callUiState = CallUiState(callState = CallStateUi.Connected, featuredStreams = ImmutableList(listOf(stream)))
        composeTestRule.waitForIdle()
        assertNotEquals(null, pipStreamRect)
    }

    @Test
    fun recordingMessage_recordingSnackbarIsDisplayed() {
        callUiState = CallUiState(userMessages = UserMessages(recordingMessage = RecordingMessage.Started()))
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun mutedMessage_mutedSnackbarIsDisplayed() {
        callUiState = CallUiState(userMessages = UserMessages(mutedMessage = MutedMessage(null)))
        val title = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_muted_by_admin, 0, "")
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
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
        featuredStreamsCount: Int = 1,
        configuration: Configuration = mockk(),
        maxWidth: Dp = 400.dp
    ): CallComponentState {
        return CallComponentState(
            featuredStreamsCount = featuredStreamsCount,
            configuration = configuration,
            maxWidth = maxWidth
        )
    }
}