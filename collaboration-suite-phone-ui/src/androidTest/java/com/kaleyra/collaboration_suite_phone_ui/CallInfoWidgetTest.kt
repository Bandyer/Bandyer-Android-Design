package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
        watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = null)
        val logo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(logo).assertIsDisplayed()
    }

    @Test
    fun watermarkTextNotNull_watermarkTextIsDisplayed() {
        watermarkInfo = WatermarkInfo(image = null, text = "watermark")
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