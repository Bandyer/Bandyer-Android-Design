package com.kaleyra.collaboration_suite_phone_ui.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.MockCallViewModelsStatesRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.component.screen.CallScreen
import com.kaleyra.collaboration_suite_phone_ui.call.component.screen.model.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.*
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.BottomSheetValue
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.FileShareComponentTag
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.LineState
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.rememberBottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.rememberBottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.component.screen.rememberCallScreenState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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