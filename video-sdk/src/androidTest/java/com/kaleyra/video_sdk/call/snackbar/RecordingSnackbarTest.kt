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
import com.kaleyra.video_sdk.common.snackbar.RecordingEndedSnackbar
import com.kaleyra.video_sdk.common.snackbar.RecordingErrorSnackbar
import com.kaleyra.video_sdk.common.snackbar.RecordingStartedSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordingSnackbarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testRecordingStartedSnackbar() {
        composeTestRule.setContent { RecordingStartedSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun testRecordingEndedSnackbar() {
        composeTestRule.setContent { RecordingEndedSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_stopped)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_stopped_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun testRecordingErrorSnackbar() {
        composeTestRule.setContent { RecordingErrorSnackbar() }
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_failed)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_recording_failed_message)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }
}