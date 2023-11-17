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

package com.kaleyra.video_sdk.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.snackbar.MutedSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MutedSnackbarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testMutedSnackbar() {
        composeTestRule.setContent { MutedSnackbar() }
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_muted_by_admin, 0, "")
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testMutedSnackbarWithAdminName() {
        val adminName = "adminUsername"
        composeTestRule.setContent { MutedSnackbar(adminName) }
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_muted_by_admin, 1, adminName)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}