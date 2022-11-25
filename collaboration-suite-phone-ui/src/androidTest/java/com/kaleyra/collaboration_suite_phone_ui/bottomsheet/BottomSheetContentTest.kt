package com.kaleyra.collaboration_suite_phone_ui.bottomsheet

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomSheetContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var contentState by mutableStateOf(BottomSheetContentState(initialSection = BottomSheetSection.CallActions, initialLineState = LineState.Expanded))

    private var isLineClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val onBackPressedDispatcher = composeTestRule.activity.onBackPressedDispatcher
            CompositionLocalProvider(LocalBackPressedDispatcher provides onBackPressedDispatcher) {
                BottomSheetContent(
                    contentState = contentState,
                    onLineClick = { isLineClicked = true }
                )
            }
        }
    }

    @Test
    fun lineStateInitialValueIsSet() {
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
    }

    @Test
    fun expandLineState_lineIsExpanded() {
        contentState = BottomSheetContentState(initialSection = BottomSheetSection.CallActions, LineState.Collapsed())
        contentState.expandLine()
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, contentState.currentLineState)
    }

    @Test
    fun collapseLineState_lineIsCollapsed() {
        contentState = BottomSheetContentState(initialSection = BottomSheetSection.CallActions, LineState.Expanded)
        contentState.collapseLine()
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
        assertEquals(LineState.Collapsed::class.java, contentState.currentLineState::class.java)
    }

    @Test
    fun userClicksLine_onLineClickInvoked() {
        composeTestRule.onAllNodes(hasClickAction()).onFirst().performClick()
        assert(isLineClicked)
    }

    @Test
    fun bottomSheetContentStateInitialSectionIsSet() {
        composeTestRule.assertSectionIsDisplayed(tag = CallActionsSection, section = BottomSheetSection.CallActions)
    }

    @Test
    fun navigateToCallActionsSection_callActionsIsDisplayed() {
        contentState = BottomSheetContentState(initialSection = BottomSheetSection.AudioOutput, initialLineState = LineState.Expanded)
        contentState.navigateToSection(BottomSheetSection.CallActions)
        composeTestRule.assertSectionIsDisplayed(tag = CallActionsSection, section = BottomSheetSection.CallActions)
    }

    @Test
    fun navigateToAudioOutputSection_audioOutputIsDisplayed() {
        contentState.navigateToSection(BottomSheetSection.AudioOutput)
        composeTestRule.assertSectionIsDisplayed(tag = AudioOutputSection, section = BottomSheetSection.AudioOutput)
    }

    @Test
    fun navigateToWhiteboardSection_whiteboardIsDisplayed() {
        contentState.navigateToSection(BottomSheetSection.Whiteboard)
        composeTestRule.assertSectionIsDisplayed(tag = WhiteboardSection, section = BottomSheetSection.Whiteboard)
    }

    @Test
    fun navigateToFileShareSection_fileShareIsDisplayed() {
        contentState.navigateToSection(BottomSheetSection.FileShare)
        composeTestRule.assertSectionIsDisplayed(tag = FileShareSection, section = BottomSheetSection.FileShare)
    }

    @Test
    fun navigateToScreenShareSection_screenShareIsDisplayed() {
        contentState.navigateToSection(BottomSheetSection.ScreenShare)
        composeTestRule.assertSectionIsDisplayed(tag = ScreenShareSection, section = BottomSheetSection.ScreenShare)
    }

    @Test
    fun userClicksOnAudioOutputButton_audioOutputIsDisplayed() {
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithContentDescription(audioOutput).performClick()
        composeTestRule.assertSectionIsDisplayed(tag = AudioOutputSection, section = BottomSheetSection.AudioOutput)
    }

    @Test
    fun userClicksOnScreenShareButton_screenShareIsDisplayed() {
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        composeTestRule.onNodeWithContentDescription(screenShare).performClick()
        composeTestRule.assertSectionIsDisplayed(tag = ScreenShareSection, section = BottomSheetSection.ScreenShare)
    }

    @Test
    fun userClicksOnWhiteboardButton_whiteboardIsDisplayed() {
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard)
        composeTestRule.onNodeWithContentDescription(whiteboard).performClick()

    }

    @Test
    fun userClicksOnFileShareButton_fileShareIsDisplayed() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        composeTestRule.onNodeWithContentDescription(fileShare).performClick()
        composeTestRule.assertSectionIsDisplayed(tag = FileShareSection, section = BottomSheetSection.FileShare)
    }

//    @Test
//    fun sheetCollapsed_lineIsCollapsed() {
//        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
//    }

//    @Test
//    fun sheetNotCollapsed_lineIsExpanded() {
//        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
//        lineState = LineState.Expanded
//        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
//    }

//    @Test
//    fun userClicksLine_sheetHalfExpand() {
//        composeTestRule.onAllNodes(hasClickAction()).onFirst().performClick()
//        composeTestRule.waitForIdle()
//        assertEquals(BottomSheetValue.Collapsed, sheetState.currentValue)
//    }

    @Test
    fun audioOutputSection_userClicksClose_callActionsDisplayed() {
        userClicksClose_callActionsDisplayed(initialSection = BottomSheetSection.AudioOutput)
    }

    @Test
    fun screenShareSection_userClicksClose_callActionsDisplayed() {
        userClicksClose_callActionsDisplayed(initialSection = BottomSheetSection.ScreenShare)
    }

    private fun userClicksClose_callActionsDisplayed(initialSection: BottomSheetSection) {
        contentState = BottomSheetContentState(initialSection = initialSection, LineState.Expanded)
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        composeTestRule.assertSectionIsDisplayed(tag = CallActionsSection, section = BottomSheetSection.CallActions)
    }

//    @Test
//    fun audioOutputSection_userPerformsBack_callActionsDisplayed() {
//        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.AudioOutput)
//    }
//
//    @Test
//    fun screenShareSection_userPerformsBack_callActionsDisplayed() {
//        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.ScreenShare)
//    }
//
//    @Test
//    fun fileShareSection_userPerformsBack_callActionsDisplayed() {
//        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.AudioOutput)
//    }
//
//    @Test
//    fun whiteboardSection_userPerformsBack_callActionsDisplayed() {
//        userPerformsBack_callActionsDisplayed(initialSection = BottomSheetSection.ScreenShare)
//    }
//
//    private fun userPerformsBack_callActionsDisplayed(initialSection: BottomSheetSection) {
//        contentState = BottomSheetContentState(initialSection = initialSection)
//        Espresso.pressBack()
//        composeTestRule.assertSectionIsDisplayed(tag = CallActionsSection, section = BottomSheetSection.CallActions)
    // add expanded sheet check
//    }
    // add test if call actions are expanded and the user presses back, the bottom sheet is collapsed

    private fun ComposeContentTestRule.assertSectionIsDisplayed(tag: String, section: BottomSheetSection) {
        onNodeWithTag(tag).assertIsDisplayed()
        assertEquals(section, contentState.currentSection)
    }
}