package com.kaleyra.video_sdk.ui.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screen.CallScreen
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import com.kaleyra.video_sdk.call.*
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetComponent
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.FileShareComponentTag
import com.kaleyra.video_sdk.call.bottomsheet.LineState
import com.kaleyra.video_sdk.call.bottomsheet.rememberBottomSheetContentState
import com.kaleyra.video_sdk.call.bottomsheet.rememberBottomSheetState
import com.kaleyra.video_sdk.call.screen.rememberCallScreenState
import com.kaleyra.video_sdk.ui.ComposeViewModelsMockTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallScreenRecreationTest : ComposeViewModelsMockTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onRecreation_stateIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            CallScreen(
                callUiState = CallUiState(callState = CallStateUi.Connected),
                callScreenState = rememberCallScreenState(
                    sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Expanded),
                    sheetContentState = rememberBottomSheetContentState(
                        initialSheetComponent = BottomSheetComponent.CallActions,
                        initialLineState = LineState.Expanded
                    )
                ),
                onBackPressed = { },
                onConfigurationChange = { },
                onThumbnailStreamClick = { },
                onThumbnailStreamDoubleClick = { },
                onFullscreenStreamClick = { },
                onUserFeedback = { _,_ -> },
                onCallEndedBack = {  },
                onFileShareVisibility = { },
                onWhiteboardVisibility = { },
                isTesting = true
            )
        }

        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        composeTestRule.onNodeWithContentDescription(fileShare).performClick()

        restorationTester.emulateSavedInstanceStateRestore()

        val fileShareAppBarTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShareAppBarTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FileShareComponentTag).assertIsDisplayed()
    }
}