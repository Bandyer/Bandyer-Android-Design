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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// TODO status bar icon must be dark if dark theme and on whiteboard and file sharing
// TODO test state is kept on rotation (1)
// TODO test collapsable first true then false, and the other way (2)
// TODO add call app bar tests (3)
// TODO add nested scroll test for file share (2)
@RunWith(AndroidJUnit4::class)
class CallScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var sheetState by mutableStateOf(BottomSheetState(BottomSheetValue.Expanded))

    private var sheetContentState by mutableStateOf(BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded))

    private var sideEffect by mutableStateOf(suspend { })

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val onBackPressedDispatcher = composeTestRule.activity.onBackPressedDispatcher
            CompositionLocalProvider(LocalBackPressedDispatcher provides onBackPressedDispatcher) {
                CallScreen(
                    sheetState = sheetState,
                    bottomSheetContentState = sheetContentState,
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
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        assertEquals(LineState.Collapsed::class, sheetContentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsed_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, sheetContentState.currentLineState)
    }

    @Test
    fun sheetNotCollapsableAndHalfExpanded_lineIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded, collapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        assertEquals(LineState.Collapsed::class, sheetContentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsableAndExpanded_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded, collapsable = false)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, sheetContentState.currentLineState)
    }

    // TODO fix
    @Test
    fun userClicksLine_sheetHalfExpand() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
//        assertEquals(BottomSheetValue.HalfExpanded, sheetState.currentValue)
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun audioOutputSection_userPerformsBack_callActionsDisplayed() {
        expandedSection_userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.AudioOutput)
    }

    @Test
    fun screenShareSection_userPerformsBack_callActionsDisplayed() {
        expandedSection_userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.ScreenShare)
    }

    @Test
    fun fileShareSection_userPerformsBack_callActionsDisplayed() {
        expandedSection_userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.FileShare)
    }

    @Test
    fun whiteboardSection_userPerformsBack_callActionsDisplayed() {
        expandedSection_userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.Whiteboard)
    }

    private fun expandedSection_userPerformsBack_callActionsDisplayed(initialSection: BottomSheetSection) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialSection, LineState.Expanded)
        assertEquals(initialSection, sheetContentState.currentSection)
        Espresso.pressBack()
        composeTestRule.onNodeWithTag(CallActionsSectionTag).assertIsDisplayed()
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    // TODO fix
    @Test
    fun callActionsSectionExpanded_userPerformsBack_sheetIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.onNodeWithTag(CallActionsSectionTag).assertIsDisplayed()
        Espresso.pressBack()
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun callActionsSectionCollapsed_userPerformsBack_activityIsFinished() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Collapsed())
        Espresso.pressBackUnconditionally()
        assertEquals(Lifecycle.State.DESTROYED, composeTestRule.activityRule.scenario.state)
    }

    @Test
    fun whiteboardSection_sheetIsExpanded() {
        sheetIsExpandedOnSection(BottomSheetSection.Whiteboard)
    }

    @Test
    fun screenShareSection_sheetIsExpanded() {
        sheetIsExpandedOnSection(BottomSheetSection.ScreenShare)
    }

    @Test
    fun fileShareSection_sheetIsExpanded() {
        sheetIsExpandedOnSection(BottomSheetSection.FileShare)
    }

    @Test
    fun audioOutputSection_sheetIsExpanded() {
        sheetIsExpandedOnSection(BottomSheetSection.AudioOutput)
    }

    private fun sheetIsExpandedOnSection(section: BottomSheetSection) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(section, LineState.Collapsed())
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun audioOutputSection_sheetHalfExpanding_callActionsSectionDisplayed() {
        sheetHalfExpanding_callActionsSectionDisplayed(BottomSheetSection.AudioOutput)
    }

    @Test
    fun screenShareSection_sheetHalfExpanding_callActionsSectionDisplayed() {
        sheetHalfExpanding_callActionsSectionDisplayed(BottomSheetSection.ScreenShare)
    }

    @Test
    fun fileShareSection_sheetHalfExpanding_callActionsSectionDisplayed() {
        sheetHalfExpanding_callActionsSectionDisplayed(BottomSheetSection.FileShare)
    }

    @Test
    fun whiteboardSection_sheetHalfExpanding_callActionsSectionDisplayed() {
        sheetHalfExpanding_callActionsSectionDisplayed(BottomSheetSection.Whiteboard)
    }

    private fun sheetHalfExpanding_callActionsSectionDisplayed(initialSection: BottomSheetSection) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialSection, LineState.Collapsed())
        sideEffect = sheetState::halfExpand
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetSection.CallActions, sheetContentState.currentSection)
    }

    @Test
    fun audioOutputSection_sheetCollapsing_callActionsSectionDisplayed() {
        sheetCollapsing_callActionsSectionDisplayed(BottomSheetSection.AudioOutput)
    }

    @Test
    fun screenShareSection_sheetCollapsing_callActionsSectionDisplayed() {
        sheetCollapsing_callActionsSectionDisplayed(BottomSheetSection.ScreenShare)
    }

    @Test
    fun fileShareSection_sheetCollapsing_callActionsSectionDisplayed() {
        sheetCollapsing_callActionsSectionDisplayed(BottomSheetSection.FileShare)
    }

    @Test
    fun whiteboardSection_sheetCollapsing_callActionsSectionDisplayed() {
        sheetCollapsing_callActionsSectionDisplayed(BottomSheetSection.Whiteboard)
    }

    private fun sheetCollapsing_callActionsSectionDisplayed(initialSection: BottomSheetSection) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(initialSection, LineState.Collapsed())
        sideEffect = sheetState::collapse
        composeTestRule.waitForIdle()
        assertEquals(BottomSheetSection.CallActions, sheetContentState.currentSection)
    }

    @Test
    fun userClicksAudioOutputAction_audioOutputIsDisplayed() {
        userClicksAction_sectionIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route),
            targetSectionTag = AudioOutputSectionTag,
            targetSection = BottomSheetSection.AudioOutput
        )
    }

    @Test
    fun userClicksScreenShareAction_screenShareIsDisplayed() {
        userClicksAction_sectionIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share),
            targetSectionTag = ScreenShareSectionTag,
            targetSection = BottomSheetSection.ScreenShare
        )
    }

    @Test
    fun userClicksWhiteboardAction_whiteboardIsDisplayed() {
        userClicksAction_sectionIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard),
            targetSectionTag = WhiteboardSectionTag,
            targetSection = BottomSheetSection.Whiteboard
        )
    }

    @Test
    fun userClicksFileShareAction_fileShareIsDisplayed() {
        userClicksAction_sectionIsDisplayed(
            actionContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share),
            targetSectionTag = FileShareSectionTag,
            targetSection = BottomSheetSection.FileShare
        )
    }

    private fun userClicksAction_sectionIsDisplayed(
        actionContentDescription: String,
        targetSectionTag: String,
        targetSection: BottomSheetSection
    ) {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithContentDescription(actionContentDescription).performClick()
        composeTestRule.onNodeWithTag(targetSectionTag).assertIsDisplayed()
        assertEquals(targetSection, sheetContentState.currentSection)
    }

    @Test
    fun sheetCollapsed_callActionsNotVisible() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        sheetContentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Collapsed())
        composeTestRule.onNodeWithTag(CallActionsSectionTag).assertDoesNotExist()
    }
}