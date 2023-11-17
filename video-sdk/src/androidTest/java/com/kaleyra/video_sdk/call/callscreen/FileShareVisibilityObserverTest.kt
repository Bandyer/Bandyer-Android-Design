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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.screen.FileShareVisibilityObserver
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetComponent
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetContentState
import com.kaleyra.video_sdk.call.bottomsheet.LineState
import com.kaleyra.video_sdk.call.screen.rememberCallScreenState
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareVisibilityObserverTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var sheetContentState by mutableStateOf(BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded))

    private var isFileShareVisible: Boolean? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareVisibilityObserver(
                callScreenState = rememberCallScreenState(sheetContentState = sheetContentState),
                onFileShareVisibility = { isFileShareVisible = it }
            )
        }
    }

    @Test
    fun currentComponentFileShare_isFileShareVisibleTrue() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.FileShare, LineState.Expanded)
        composeTestRule.waitForIdle()
        Assert.assertEquals(true, isFileShareVisible)
    }

    @Test
    fun currentComponentOther_isFileShareVisibleFalse() {
        sheetContentState = BottomSheetContentState(BottomSheetComponent.CallActions, LineState.Expanded)
        composeTestRule.waitForIdle()
        Assert.assertEquals(false, isFileShareVisible)
    }

}