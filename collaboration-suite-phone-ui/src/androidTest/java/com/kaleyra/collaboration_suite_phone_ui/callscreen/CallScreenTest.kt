package com.kaleyra.collaboration_suite_phone_ui.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.MockCallViewModelsStatesRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.mockAudioDevices
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.findBackButton
import io.mockk.every
import io.mockk.mockkConstructor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mockCallViewModelsStatesRule = MockCallViewModelsStatesRule()

    private var callUiState by mutableStateOf(CallUiState())

    private var sheetState by mutableStateOf(BottomSheetState(BottomSheetValue.Expanded))

    private var sheetContentState by mutableStateOf(BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded))

    private var sideEffect by mutableStateOf(suspend { })

    private var thumbnailClickedStream: StreamUi? = null

    private var backPressed = false

    private var answerClicked = false

    private var declineClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val onBackPressedDispatcher = composeTestRule.activity.onBackPressedDispatcher
            CompositionLocalProvider(LocalBackPressedDispatcher provides onBackPressedDispatcher) {
                CallScreen(
                    callScreenState = rememberCallScreenState(
                        callUiState = callUiState,
                        sheetState = sheetState,
                        sheetContentState = sheetContentState
                    ),
                    onThumbnailStreamClick = { thumbnailClickedStream = it },
                    onBackPressed = { backPressed = true },
                    onAnswerClick = { answerClicked = true },
                    onDeclineClick = { declineClicked =  true }
                )
                LaunchedEffect(sideEffect) {
                    sideEffect.invoke()
                }
            }
        }
    }

    @Test
    fun sheetCollapsed_lineIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        assertEquals(LineState.Collapsed::class, sheetContentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsed_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, sheetContentState.currentLineState)
    }

    @Test
    fun sheetNotCollapsableAndHalfExpanded_lineIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded, isCollapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        assertEquals(LineState.Collapsed::class, sheetContentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsableAndExpanded_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded, isCollapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, sheetContentState.currentLineState)
    }

    @Test
    fun sheetCollapsed_userClicksLine_sheetHalfExpand() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun audioOutputComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.ScreenShare)
    }

    @Test
    fun fileShareComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_userPerformsBack_callActionsDisplayed() {
        expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent = BottomSheetComponent.Whiteboard)
    }

    private fun expandedComponent_userPerformsBack_callActionsDisplayed(initialComponent: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialComponent, LineState.Expanded)
        assertEquals(initialComponent, sheetContentState.currentComponent)
        Espresso.pressBack()
        composeTestRule.onNodeWithTag(CallActionsComponentTag).assertIsDisplayed()
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun callActionsComponentExpanded_userPerformsBack_sheetIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(CallActionsComponentTag).assertIsDisplayed()
        Espresso.pressBack()
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun callActionsComponentCollapsed_userPerformsBack_activityIsFinished() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        Espresso.pressBackUnconditionally()
        assertEquals(Lifecycle.State.DESTROYED, composeTestRule.activityRule.scenario.state)
    }

    @Test
    fun userClicksWhiteboardAction_sheetIsExpanded() {
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard)
        userClicksAction_sheetIsExpanded(whiteboard)
    }

    @Test
    fun userClicksScreenShareAction_sheetIsExpanded() {
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        userClicksAction_sheetIsExpanded(screenShare)
    }

    @Test
    fun userClicksFileShareAction_sheetIsExpanded() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        userClicksAction_sheetIsExpanded(fileShare)
    }

    @Test
    fun userClicksAudioOutputAction_sheetIsExpanded() {
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        userClicksAction_sheetIsExpanded(audioOutput)
    }

    private fun userClicksAction_sheetIsExpanded(actionDescription: String) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithContentDescription(actionDescription).performClick()
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun audioOutputComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.ScreenShare)
    }

    @Test
    fun fileShareComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_sheetHalfExpanding_callActionsComponentDisplayed() {
        sheetHalfExpanding_callActionsComponentDisplayed(BottomSheetComponent.Whiteboard)
    }

    private fun sheetHalfExpanding_callActionsComponentDisplayed(initialComponent: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialComponent, LineState.Collapsed())
        sideEffect = sheetState::halfExpand
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetComponent.CallActions, sheetContentState.currentComponent)
    }

    @Test
    fun audioOutputComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.ScreenShare)
    }

    @Test
    fun fileShareComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_sheetCollapsing_callActionsComponentDisplayed() {
        sheetCollapsing_callActionsComponentDisplayed(BottomSheetComponent.Whiteboard)
    }

    private fun sheetCollapsing_callActionsComponentDisplayed(initialComponent: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialComponent, LineState.Collapsed())
        sideEffect = sheetState::collapse
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetComponent.CallActions, sheetContentState.currentComponent)
    }

    @Test
    fun userClicksAudioOutputAction_audioOutputIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route),
            targetComponentTag = AudioOutputComponentTag,
            targetComponent = BottomSheetComponent.AudioOutput
        )
    }

    @Test
    fun userClicksScreenShareAction_screenShareIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share),
            targetComponentTag = ScreenShareComponentTag,
            targetComponent = BottomSheetComponent.ScreenShare
        )
    }

    @Test
    fun userClicksWhiteboardAction_whiteboardIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard),
            targetComponentTag = WhiteboardComponentTag,
            targetComponent = BottomSheetComponent.Whiteboard
        )
    }

    @Test
    fun userClicksFileShareAction_fileShareIsDisplayed() {
        userClicksAction_componentIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share),
            targetComponentTag = FileShareComponentTag,
            targetComponent = BottomSheetComponent.FileShare
        )
    }

    private fun userClicksAction_componentIsDisplayed(
        actionContentDescription: String,
        targetComponentTag: String,
        targetComponent: BottomSheetComponent
    ) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithContentDescription(actionContentDescription).performClick()
        composeTestRule.onNodeWithTag(targetComponentTag).assertIsDisplayed()
        assertEquals(targetComponent, sheetContentState.currentComponent)
    }

    @Test
    fun sheetCollapsed_callActionsNotVisible() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(CallActionsComponentTag).assertDoesNotExist()
    }

    @Test
    fun fileShareComponentExpanded_fileShareAppBarDisplayed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.FileShare, LineState.Expanded)
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithTag(CallScreenAppBarTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }

    @Test
    fun whiteboardComponentExpanded_whiteboardAppBarDisplayed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.Whiteboard, LineState.Expanded)
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithTag(CallScreenAppBarTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
    }

    @Test
    fun callActionsComponentExpanded_appBarNotDisplayed() {
        componentExpanded_appBarNotDisplayed(BottomSheetComponent.CallActions)
    }

    @Test
    fun audioOutputComponentExpanded_appBarNotDisplayed() {
        componentExpanded_appBarNotDisplayed(BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponentExpanded_appBarNotDisplayed() {
        componentExpanded_appBarNotDisplayed(BottomSheetComponent.ScreenShare)
    }

    private fun componentExpanded_appBarNotDisplayed(component: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(component, LineState.Expanded)
        composeTestRule.onNodeWithTag(CallScreenAppBarTag).assertDoesNotExist()
    }

    @Test
    fun sheetHidden_thumbnailStreamsAreNotDisplayed() {
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.Hidden, areVisible = false)
    }

    @Test
    fun sheetCollapsed_thumbnailStreamsAreDisplayed() {
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.Collapsed, areVisible = true)
    }

    @Test
    fun sheetHalfExpanded_thumbnailStreamsAreDisplayed() {
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.HalfExpanded, areVisible = true)
    }

    @Test
    fun sheetExpanded_thumbnailStreamsAreDisplayed() {
        checkThumbnailStreamsVisibility(sheetValue = BottomSheetValue.Expanded, areVisible = true)
    }

    private fun checkThumbnailStreamsVisibility(sheetValue: BottomSheetValue, areVisible: Boolean) {
        sheetState = BottomSheetState(initialValue = sheetValue)
        callUiState = CallUiState(thumbnailStreams = ImmutableList(listOf(streamUiMock)))
        composeTestRule.onAllNodesWithTag(ThumbnailTag).assertCountEquals(if (areVisible) 1 else 0)
    }

    @Test
    fun audioOutputComponent_userClicksAudioDevice_sheetHalfExpand() {
        mockkConstructor(AudioOutputViewModel::class)
        every { anyConstructed<AudioOutputViewModel>().uiState } returns MutableStateFlow(
            AudioOutputUiState(audioDeviceList = mockAudioDevices, playingDeviceId = "id")
        )
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.AudioOutput, LineState.Collapsed())
        val loudspeaker = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route_loudspeaker)
        composeTestRule.onNodeWithText(loudspeaker).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun screenShareComponent_userClicksScreenShareDevice_sheetHalfExpand() {
        mockkConstructor(ScreenShareViewModel::class)
        every { anyConstructed<ScreenShareViewModel>().uiState } returns MutableStateFlow(
            ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)))
        )
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.ScreenShare, LineState.Collapsed())
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun virtualBackgroundComponent_userClicksVirtualBackground_sheetHalfExpand() {
        mockkConstructor(VirtualBackgroundViewModel::class)
        every { anyConstructed<VirtualBackgroundViewModel>().uiState } returns MutableStateFlow(
            VirtualBackgroundUiState(backgrounds = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("id1"), VirtualBackgroundUi.Image("id2"))))
        )
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.VirtualBackground, LineState.Collapsed())
        val none = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_none)
        composeTestRule.onNodeWithText(none).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun userClicksThumbnail_onThumbnailStreamClickInvoked() {
        callUiState = CallUiState(thumbnailStreams = ImmutableList(listOf(streamUiMock)))
        composeTestRule.onNodeWithTag(ThumbnailTag).performClick()
        assertEquals(streamUiMock, thumbnailClickedStream)
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(backPressed)
    }

    @Test
    fun userClicksAnswerButton_onAnswerClickInvoked() {
        sheetState = BottomSheetState(BottomSheetValue.Hidden)
        callUiState = callUiState.copy(callInfo = callInfoMock.copy(callState = CallState.Ringing))
        val answer = composeTestRule.activity.getString(R.string.kaleyra_ringing_answer)
        composeTestRule.onAllNodesWithContentDescription(answer).onFirst().performClick()
        assert(answerClicked)
    }

    @Test
    fun userClicksDeclineButton_onDeclineClickInvoked() {
        sheetState = BottomSheetState(BottomSheetValue.Hidden)
        callUiState = callUiState.copy(callInfo = callInfoMock.copy(callState = CallState.Ringing))
        val decline = composeTestRule.activity.getString(R.string.kaleyra_ringing_decline)
        composeTestRule.onAllNodesWithContentDescription(decline).onFirst().performClick()
        assert(declineClicked)
    }
}