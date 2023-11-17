/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenRecreationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mockCallViewModelsStatesRule = com.kaleyra.video_sdk.MockCallViewModelsStatesRule()

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
                onCallEndedBack = {},
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