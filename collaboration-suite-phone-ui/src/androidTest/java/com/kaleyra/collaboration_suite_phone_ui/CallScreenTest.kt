package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.LocalBackPressedDispatcher
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareAppBarTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.rememberCallScreenState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardAppBarTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// TODO status bar icon must be dark if dark theme and on whiteboard and file sharing
// TODO add call app bar tests (1)
// TODO add nested scroll test for file share (2)
@RunWith(AndroidJUnit4::class)
class CallScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var sheetState by mutableStateOf(BottomSheetState(BottomSheetValue.Expanded))

    private var sheetContentState by mutableStateOf(BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded))

    private var sideEffect by mutableStateOf(suspend { })

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val onBackPressedDispatcher = composeTestRule.activity.onBackPressedDispatcher
            CompositionLocalProvider(LocalBackPressedDispatcher provides onBackPressedDispatcher) {
                CallScreen(
                    callScreenState = rememberCallScreenState(
                        sheetState = sheetState,
                        sheetContentState = sheetContentState
                    )
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
        sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded, collapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        assertEquals(LineState.Collapsed::class, sheetContentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsableAndExpanded_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded, collapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, sheetContentState.currentLineState)
    }

    // TODO fix
    @Test
    fun userClicksLine_sheetHalfExpand() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag("Prova").performClick()
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
    fun whiteboardComponent_sheetIsExpanded() {
        sheetIsExpandedOnComponent(BottomSheetComponent.Whiteboard)
    }

    @Test
    fun screenShareComponent_sheetIsExpanded() {
        sheetIsExpandedOnComponent(BottomSheetComponent.ScreenShare)
    }

    @Test
    fun fileShareComponent_sheetIsExpanded() {
        sheetIsExpandedOnComponent(BottomSheetComponent.FileShare)
    }

    @Test
    fun audioOutputComponent_sheetIsExpanded() {
        sheetIsExpandedOnComponent(BottomSheetComponent.AudioOutput)
    }

    private fun sheetIsExpandedOnComponent(component: BottomSheetComponent) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(component, LineState.Collapsed())
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
    fun fileShareComponent_fileShareAppBarDisplayed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.FileShare, LineState.Expanded)
        composeTestRule.onNodeWithTag(FileShareAppBarTag).assertIsDisplayed()
    }

    @Test
    fun whiteboardComponent_whiteboardAppBarDisplayed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetComponent.Whiteboard, LineState.Expanded)
        composeTestRule.onNodeWithTag(WhiteboardAppBarTag).assertIsDisplayed()
    }

//    @Test
//    fun fileShareComponent_scroll() {
//        mockkConstructor(FileShareViewModel::class)
//        every { anyConstructed<FileShareViewModel>().initialState() } returns
//                FileShareUiState(
//                    transferList = ImmutableList(listOf(
//                        mockDownloadTransfer.copy(id = "0", state = TransferUi.State.Success(Uri.EMPTY)),
//                        mockUploadTransfer.copy(id = "1"),
//                        mockUploadTransfer.copy(id = "2"),
//                        mockUploadTransfer.copy(id = "3"),
//                        mockUploadTransfer.copy(id = "4"),
//                        mockUploadTransfer.copy(id = "5"),
//                        mockUploadTransfer.copy(id = "6"),
//                        mockUploadTransfer.copy(id = "7"),
//                        mockUploadTransfer.copy(id = "8"),
//                        mockUploadTransfer.copy(id = "9"),
//                        mockUploadTransfer.copy(id = "10"),
//                        mockUploadTransfer.copy(id = "11"),
//                        mockUploadTransfer.copy(id = "12"),
//                        mockUploadTransfer.copy(id = "13"),
//                        mockUploadTransfer.copy(id = "14"),
//                        mockUploadTransfer.copy(id = "15"),
//                        mockUploadTransfer.copy(id = "16"),
//                        mockUploadTransfer.copy(id = "17"),
//                        mockUploadTransfer.copy(id = "18"),
//                        mockUploadTransfer.copy(id = "19"),
//                        mockUploadTransfer.copy(id = "20"),
//                        mockUploadTransfer.copy(id = "21"),
//                        mockUploadTransfer.copy(id = "22"),
//                        mockUploadTransfer.copy(id = "23"),
//                        mockUploadTransfer.copy(id = "24"),
//                        mockUploadTransfer.copy(id = "25"),
//                        mockUploadTransfer.copy(id = "26")
//                    ))
//                )
//        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
//        sheetContentState = BottomSheetContentState(BottomSheetComponent.FileShare, LineState.Expanded)
//        composeTestRule.waitForIdle()
//        runBlocking {
//            delay(3000)
//            assertEquals(BottomSheetComponent.FileShare, sheetContentState.currentComponent)
//        }
//    }
}