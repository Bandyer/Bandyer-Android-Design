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

package com.kaleyra.video_sdk.ui.call

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.CallInfoWidget
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.ui.findBackButton
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class CallInfoWidgetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var title by mutableStateOf("")

    private var subtitle by mutableStateOf("")

    private var watermarkInfo by mutableStateOf(WatermarkInfo())

    private var isRecording by mutableStateOf(false)

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallInfoWidget(
                title = title,
                subtitle = subtitle,
                watermarkInfo = watermarkInfo,
                recording = isRecording,
                onBackPressed = { isBackPressed = true }
            )
        }
    }

    @After
    fun tearDown() {
        title = ""
        subtitle = ""
        watermarkInfo = WatermarkInfo()
        isRecording = false
        isBackPressed = false
    }

    @Test
    fun backButtonIsDisplayed() {
        composeTestRule.findBackButton().assertIsDisplayed()
    }

    @Test
    fun userClicksBack_onBackPressedInvoked() {
        composeTestRule.findBackButton().performClick()
        assert(isBackPressed)
    }

    @Test
    fun recordingFalse_recordingLabelDoesNotExists() {
        isRecording = false
        findRecordingLabel().assertDoesNotExist()
    }

    @Test
    fun recordingTrue_recordingLabelIsDisplayed() {
        isRecording = true
        findRecordingLabel().assertIsDisplayed()
    }

    @Test
    fun watermarkImageNotNull_watermarkImageIsDisplayed() {
        val uri = Uri.parse("com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo")
        watermarkInfo = WatermarkInfo(logo = Logo(uri, uri), text = null)
        val logoContentDescr = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(logoContentDescr).assertIsDisplayed()
    }

    @Test
    fun watermarkTextNotNull_watermarkTextIsDisplayed() {
        watermarkInfo = WatermarkInfo(logo = null, text = "watermark")
        composeTestRule.onNodeWithText("watermark").assertIsDisplayed()
    }

    // NB: The title is actually an AndroidView, because there is not text ellipsize in compose
    @Test
    fun titleIsDisplayed() {
        title = "title"
        composeTestRule.onNodeWithContentDescription(title).assertIsDisplayed()
    }

    @Test
    fun subtitleNotNull_subtitleIsDisplayed() {
        subtitle = "subtitle"
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    private fun findRecordingLabel(): SemanticsNodeInteraction {
        val rec = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec).uppercase()
        return composeTestRule.onNodeWithText(rec)
    }

}