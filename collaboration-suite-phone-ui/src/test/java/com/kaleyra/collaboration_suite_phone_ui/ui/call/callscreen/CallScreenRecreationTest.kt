package com.kaleyra.collaboration_suite_phone_ui.ui.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.ui.call.MockCallViewModelsStatesRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class CallScreenRecreationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mockCallViewModelsStatesRule = MockCallViewModelsStatesRule()

    @Test
    fun onRecreation_stateIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent { CallScreen(
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
                onUserFeedback = {_,_ ->}
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