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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.view.MaxFileSizeDialog
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MaxFileSizeDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    var isDismissed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
           MaxFileSizeDialog {
               isDismissed = true
           }
        }
    }

    @After
    fun tearDown() {
        isDismissed = false
    }

    @Test
    fun fileShareLimitTitleIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_max_bytes_dialog_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareLimitDescriptionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_max_bytes_dialog_descr)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksOnOk_dialogIsDismissed() {
        val ok = composeTestRule.activity.getString(R.string.kaleyra_button_ok)
        composeTestRule.onNodeWithText(ok).performClick()
        assert(isDismissed)
    }
}