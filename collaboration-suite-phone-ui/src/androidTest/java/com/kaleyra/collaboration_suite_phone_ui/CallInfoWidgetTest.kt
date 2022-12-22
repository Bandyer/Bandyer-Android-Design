package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Watermark
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallInfoWidgetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callInfo by mutableStateOf(callInfoMock)

    private var showWatermark by mutableStateOf(false)

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallInfoWidget(
                onBackPressed = { isBackPressed = true },
                callInfo = callInfo,
                showWatermark = showWatermark
            )
        }
    }

    @Test
    fun backButtonIsDisplayed() {
        findBackButton().assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        findHeaderTitle().assertIsDisplayed()
    }

    @Test
    fun subtitleIsDisplayed() {
        findHeaderSubtitle().assertIsDisplayed()
    }

    @Test
    fun userClicksBack_onBackPressedInvoked() {
        findBackButton().performClick()
        assert(isBackPressed)
    }

    @Test
    fun recordingFalse_recordingLabelDoesNotExists() {
        callInfo = callInfoMock.copy(isRecording = false)
        findRecordingLabel().assertDoesNotExist()
    }

    @Test
    fun recordingTrue_recordingLabelIsDisplayed() {
        callInfo = callInfoMock.copy(isRecording = true)
        findRecordingLabel().assertIsDisplayed()
    }

    @Test
    fun watermarkImageNotNull_watermarkImageIsDisplayed() {
        showWatermark = true
        callInfo = callInfoMock.copy(
            watermark = Watermark(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = null)
        )
        findWatermarkImage().assertIsDisplayed()
    }

    @Test
    fun watermarkTextNotNull_watermarkTextIsDisplayed() {
        showWatermark = true
        callInfo = callInfoMock.copy(watermark = Watermark(image = null, text = "watermark"))
        composeTestRule.onNodeWithText("watermark").assertIsDisplayed()
    }

    @Test
    fun showWatermarkTrue_watermarkIsDisplayed() {
        callInfo = callInfoMock.copy(watermark = Watermark(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "watermark"))
        showWatermark = true
        findWatermarkImage().assertIsDisplayed()
        composeTestRule.onNodeWithText("watermark").assertIsDisplayed()
    }

    @Test
    fun showWatermarkFalse_watermarkDoesNotExists() {
        callInfo = callInfoMock.copy(watermark = Watermark(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "watermark"))
        showWatermark = false
        findWatermarkImage().assertDoesNotExist()
        composeTestRule.onNodeWithText("watermark").assertDoesNotExist()
    }

    @Test
    fun showWatermarkTrue_titleIsDisplayedBelowWatermark() {
        callInfo = callInfoMock.copy(watermark = Watermark(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "watermark"))
        showWatermark = true
        val titleTop = findHeaderTitle().getBoundsInRoot().top
        val watermarkBottom = findWatermarkImage().getBoundsInRoot().bottom
        assert(titleTop > watermarkBottom)
    }

    @Test
    fun showWatermarkFalse_titleIsDisplayedToEndOfBackButton() {
        callInfo = callInfoMock.copy(watermark = Watermark(image = null, text = "watermark"))
        showWatermark = false
        val subtitleLeft = findHeaderTitle().getBoundsInRoot().left
        val backRight = findBackButton().getBoundsInRoot().right
        assert(subtitleLeft > backRight)
    }

    private fun findBackButton(): SemanticsNodeInteraction {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        return composeTestRule.onNodeWithContentDescription(back)
    }

    private fun findRecordingLabel(): SemanticsNodeInteraction {
        val rec = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec).uppercase()
        return composeTestRule.onNodeWithText(rec)
    }

    private fun findWatermarkImage(): SemanticsNodeInteraction {
        val logo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        return composeTestRule.onNodeWithContentDescription(logo)
    }

    private fun findHeaderTitle(): SemanticsNodeInteraction = composeTestRule.onNodeWithContentDescription("title")

    private fun findHeaderSubtitle(): SemanticsNodeInteraction = composeTestRule.onNodeWithText("subtitle")
}