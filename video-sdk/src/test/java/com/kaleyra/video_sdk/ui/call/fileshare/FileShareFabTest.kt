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

package com.kaleyra.video_sdk.ui.call.fileshare

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.view.FileShareFab
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileShareFabTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var collapsed by mutableStateOf(false)

    private var isClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareFab(
                collapsed = collapsed,
                onClick = { isClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        collapsed = false
        isClicked = false
    }

    @Test
    fun collapsedTrue_textNotExists() {
        collapsed = true
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        val addDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(addDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(add).assertDoesNotExist()
    }

    @Test
    fun collapsedFalse_textDisplayed() {
        collapsed = false
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        val addDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(addDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(add).assertIsDisplayed()
    }
}