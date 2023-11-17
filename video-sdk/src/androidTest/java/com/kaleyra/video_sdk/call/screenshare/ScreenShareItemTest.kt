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

package com.kaleyra.video_sdk.screenshare

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.call.screenshare.view.ScreenShareItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenShareItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var screenShareTarget by mutableStateOf(ScreenShareTargetUi.Device)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ScreenShareItem(screenShareTarget = screenShareTarget)
        }
    }

    @After
    fun tearDown() {
        screenShareTarget = ScreenShareTargetUi.Device
    }

    @Test
    fun deviceScreenShare_deviceScreenShareTextDisplayed() {
        screenShareTarget = ScreenShareTargetUi.Device
        val fullDevice = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(fullDevice).assertIsDisplayed()
    }

    @Test
    fun appScreenShareTarget_appScreenShareTextDisplayed() {
        screenShareTarget = ScreenShareTargetUi.Application
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).assertIsDisplayed()
    }
}