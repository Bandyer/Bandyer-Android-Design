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

package com.kaleyra.video_sdk.ui.call.screenshare

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screenshare.ScreenShareComponent
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenShareComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<ScreenShareTargetUi>()))

    private var screenShareTarget: ScreenShareTargetUi? = null

    private var isCloseClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ScreenShareComponent(
                uiState = ScreenShareUiState(targetList = items),
                onItemClick = { screenShareTarget = it },
                onCloseClick = { isCloseClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        screenShareTarget = null
        isCloseClicked = false
    }

    @Test
    fun screenShareTitleDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isCloseClicked)
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).performClick()
        assertEquals(ScreenShareTargetUi.Application, screenShareTarget)
    }
}