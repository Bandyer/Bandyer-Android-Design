package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sheetCollapsed_lineIsCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        val contentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        assertEquals(LineState.Collapsed::class, contentState.currentLineState::class)
    }

    @Test
    fun sheetNotCollapsed_lineIsExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        val contentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, contentState.currentLineState)
    }

    @Test
    fun sheetNotCollapsableAndHalfExpanded_lineIsCollapsed() {
        val sheetState =
            BottomSheetState(initialValue = BottomSheetValue.HalfExpanded, collapsable = false)
        val contentState =
            BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        runBlocking {
            val lineState = snapshotFlow { contentState.currentLineState }.first()
            assertEquals(LineState.Collapsed::class, lineState::class)
        }
    }

    @Test
    fun sheetNotCollapsableAndExpanded_lineIsExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded, collapsable = false)
        val contentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, contentState.currentLineState)
    }

    @Test
    fun userClicksLine_sheetHalfExpand() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        val contentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        runBlocking {
            val sheetStateValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.HalfExpanded, sheetStateValue)
        }
    }

    @Test
    fun audioOutputSection_userPerformsBack_callActionsDisplayed() {
        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.AudioOutput)
    }

    @Test
    fun screenShareSection_userPerformsBack_callActionsDisplayed() {
        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.ScreenShare)
    }

    @Test
    fun fileShareSection_userPerformsBack_callActionsDisplayed() {
        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.FileShare)
    }

    @Test
    fun whiteboardSection_userPerformsBack_callActionsDisplayed() {
        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.Whiteboard)
    }

    private fun userPerformsBack_callActionsDisplayed(initialSection: BottomSheetSection) {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded, collapsable = false)
        val contentState = BottomSheetContentState(initialSection, LineState.Expanded)
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        assertEquals(initialSection, contentState.currentSection)
        Espresso.pressBack()
        composeTestRule.onNodeWithTag(CallActionsSectionTag).assertIsDisplayed()
        assertEquals(BottomSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun callActionsSectionExpanded_userPerformsBack_sheetIsCollapsed() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        val contentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Expanded)
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        composeTestRule.onNodeWithTag(CallActionsSectionTag).assertIsDisplayed()
        Espresso.pressBack()
        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun callActionsSectionCollapsed_userPerformsBack_activityIsFinished() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.Collapsed)
        val contentState = BottomSheetContentState(BottomSheetSection.CallActions, LineState.Collapsed())
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        Espresso.pressBackUnconditionally()
        assertEquals(Lifecycle.State.DESTROYED, composeTestRule.activityRule.scenario.state)
    }

    // Every section different from call actions must be expanded
    // if half expanding the section must be switched to call actions
    // status bar icon must be dark if dark theme and on whiteboard and file sharing

    @Test
    fun whiteboardSection_sheetIsExpanded() {
        val sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded)
        val contentState = BottomSheetContentState(BottomSheetSection.Whiteboard, LineState.Collapsed())
        composeTestRule.setCallScreenContent(
            sheetState = sheetState,
            contentState = contentState
        )
        composeTestRule.waitForIdle()
        runBlocking {
            delay(4000)
            val sheetValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(BottomSheetValue.Expanded, sheetValue)
        }
    }

    private fun ComposeContentTestRule.setCallScreenContent(
        sheetState: BottomSheetState,
        contentState: BottomSheetContentState
    ) {
        composeTestRule.setContent {
            val onBackPressedDispatcher = composeTestRule.activity.onBackPressedDispatcher
            CompositionLocalProvider(LocalBackPressedDispatcher provides onBackPressedDispatcher) {
                CallScreen(
                    sheetState = sheetState,
                    bottomSheetContentState = contentState,
                )
            }
        }
    }
}