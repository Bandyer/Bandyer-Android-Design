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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VirtualBackgroundComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<VirtualBackgroundUi>()))

    private var backgroundClicked: VirtualBackgroundUi? = null

    private var isCloseClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            VirtualBackgroundComponent(
                uiState = VirtualBackgroundUiState(backgroundList = items),
                onItemClick = { backgroundClicked = it },
                onCloseClick = { isCloseClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        backgroundClicked = null
        isCloseClicked = false
    }

    @Test
    fun virtualBackgroundTitleDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
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
        items = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("id")))
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).performClick()
        Assert.assertEquals(VirtualBackgroundUi.Blur("id"), backgroundClicked)
    }
}