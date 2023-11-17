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

package com.kaleyra.video_sdk.call.virtualbackground

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.view.VirtualBackgroundItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VirtualBackgroundItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var background by mutableStateOf<VirtualBackgroundUi>(VirtualBackgroundUi.None)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            VirtualBackgroundItem(background = background, selected = false)
        }
    }

    @After
    fun tearDown() {
        background = VirtualBackgroundUi.None
    }

    @Test
    fun noVirtualBackground_noVirtualBackgroundTextDisplayed() {
        background = VirtualBackgroundUi.None
        val none = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_none)
        composeTestRule.onNodeWithText(none).assertIsDisplayed()
    }

    @Test
    fun blurVirtualBackground_blurVirtualBackgroundTextDisplayed() {
        background = VirtualBackgroundUi.Blur("id")
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).assertIsDisplayed()
    }

    @Test
    fun imageVirtualBackground_imageVirtualBackgroundTextDisplayed() {
        background = VirtualBackgroundUi.Image("id")
        val image = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_image)
        composeTestRule.onNodeWithText(image).assertIsDisplayed()
    }
}