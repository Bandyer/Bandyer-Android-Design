package com.kaleyra.collaboration_suite_phone_ui.ui.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.screen.CallScreen
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.*
import com.kaleyra.collaboration_suite_phone_ui.call.common.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetValue
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.FileShareComponentTag
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.LineState
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.rememberBottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.rememberBottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.screen.rememberCallScreenState
import com.kaleyra.collaboration_suite_phone_ui.ui.ComposeViewModelsMockTest
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
                onThumbnailStreamClick = { },
                onThumbnailStreamDoubleClick = { },
                onFullscreenStreamClick = { },
                onFileShareVisibility = { },
                onWhiteboardVisibility = { },
                onConfigurationChange = { },
                onBackPressed = { },
                onFinishActivity = {},
                onUserFeedback = {_,_ ->},
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