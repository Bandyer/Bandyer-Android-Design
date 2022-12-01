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

    private var contentState by mutableStateOf(BottomSheetContentState(initialComponent = BottomSheetComponent.CallActions, initialLineState = LineState.Expanded))

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
        contentState = BottomSheetContentState(initialComponent = BottomSheetComponent.CallActions, LineState.Collapsed())
        contentState.expandLine()
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
        assertEquals(LineState.Expanded, contentState.currentLineState)
    }

    @Test
    fun collapseLineState_lineIsCollapsed() {
        contentState = BottomSheetContentState(initialComponent = BottomSheetComponent.CallActions, LineState.Expanded)
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
    fun bottomSheetContentStateInitialComponentIsSet() {
        composeTestRule.assertComponentIsDisplayed(tag = CallActionsComponentTag, component = BottomSheetComponent.CallActions)
    }

    @Test
    fun navigateToCallActionsComponent_callActionsIsDisplayed() {
        contentState = BottomSheetContentState(initialComponent = BottomSheetComponent.AudioOutput, initialLineState = LineState.Expanded)
        contentState.navigateToComponent(BottomSheetComponent.CallActions)
        composeTestRule.assertComponentIsDisplayed(tag = CallActionsComponentTag, component = BottomSheetComponent.CallActions)
    }

    @Test
    fun navigateToAudioOutputComponent_audioOutputIsDisplayed() {
        contentState.navigateToComponent(BottomSheetComponent.AudioOutput)
        composeTestRule.assertComponentIsDisplayed(tag = AudioOutputComponentTag, component = BottomSheetComponent.AudioOutput)
    }

    @Test
    fun navigateToWhiteboardComponent_whiteboardIsDisplayed() {
        contentState.navigateToComponent(BottomSheetComponent.Whiteboard)
        composeTestRule.assertComponentIsDisplayed(tag = WhiteboardComponentTag, component = BottomSheetComponent.Whiteboard)
    }

    @Test
    fun navigateToFileShareComponent_fileShareIsDisplayed() {
        contentState.navigateToComponent(BottomSheetComponent.FileShare)
        composeTestRule.assertComponentIsDisplayed(tag = FileShareComponentTag, component = BottomSheetComponent.FileShare)
    }

    @Test
    fun navigateToScreenShareComponent_screenShareIsDisplayed() {
        contentState.navigateToComponent(BottomSheetComponent.ScreenShare)
        composeTestRule.assertComponentIsDisplayed(tag = ScreenShareComponentTag, component = BottomSheetComponent.ScreenShare)
    }

    @Test
    fun userClicksOnAudioOutputButton_audioOutputIsDisplayed() {
        val audioOutput = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithContentDescription(audioOutput).performClick()
        composeTestRule.assertComponentIsDisplayed(tag = AudioOutputComponentTag, component = BottomSheetComponent.AudioOutput)
    }

    @Test
    fun userClicksOnScreenShareButton_screenShareIsDisplayed() {
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        composeTestRule.onNodeWithContentDescription(screenShare).performClick()
        composeTestRule.assertComponentIsDisplayed(tag = ScreenShareComponentTag, component = BottomSheetComponent.ScreenShare)
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
        composeTestRule.assertComponentIsDisplayed(tag = FileShareComponentTag, component = BottomSheetComponent.FileShare)
    }

    @Test
    fun audioOutputComponent_userClicksClose_callActionsDisplayed() {
        userClicksClose_callActionsDisplayed(initialComponent = BottomSheetComponent.AudioOutput)
    }

    @Test
    fun screenShareComponent_userClicksClose_callActionsDisplayed() {
        userClicksClose_callActionsDisplayed(initialComponent = BottomSheetComponent.ScreenShare)
    }

    private fun userClicksClose_callActionsDisplayed(initialComponent: BottomSheetComponent) {
        contentState = BottomSheetContentState(initialComponent = initialComponent, LineState.Expanded)
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        composeTestRule.assertComponentIsDisplayed(tag = CallActionsComponentTag, component = BottomSheetComponent.CallActions)
    }

    private fun ComposeContentTestRule.assertComponentIsDisplayed(tag: String, component: BottomSheetComponent) {
        onNodeWithTag(tag).assertIsDisplayed()
        assertEquals(component, contentState.currentComponent)
    }
}