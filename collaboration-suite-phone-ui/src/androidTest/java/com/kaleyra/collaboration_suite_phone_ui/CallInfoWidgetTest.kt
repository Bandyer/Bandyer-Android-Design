package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallInfoWidgetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isRecording by mutableStateOf(false)

    private var showWatermark by mutableStateOf(false)

    private var showHeader by mutableStateOf(false)

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallInfoWidget(
                onBackPressed = { isBackPressed = true },
                callInfo = CallInfoUi(
                    headerTitle = "title",
                    headerSubtitle = "subtitle",
                    watermarkImage = painterResource(id = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo),
                    watermarkText = "watermark"
                ),
                watermark = showWatermark,
                header = showHeader,
                recording = isRecording
            )
        }
    }

    @Test
    fun backButtonIsDisplayed() {
        findBackButton().assertIsDisplayed()
    }

    @Test
    fun userClicksBack_onBackPressedInvoked() {
        findBackButton().performClick()
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
    fun watermarkTrue_watermarkIsDisplayed() {
        showWatermark = true
        findWatermark().assertIsDisplayed()
    }

    @Test
    fun watermarkFalse_watermarkDoesNotExists() {
        showWatermark = false
        findWatermark().assertDoesNotExist()
    }

    @Test
    fun headerTrue_headerIsDisplayed() {
        showHeader = true
        findHeaderTitle().check(matches(isDisplayed()))
        findHeaderSubtitle().assertIsDisplayed()
    }

    @Test
    fun headerFalse_headerDoesNotExists() {
        showHeader = false
        findHeaderTitle().check(doesNotExist())
        findHeaderSubtitle().assertDoesNotExist()
    }

//    @Test
//    fun watermarkTrue_headerIsDisplayedBelowWatermark() {
//        showWatermark = true
//        showHeader = true
//        val subtitleTop = findHeaderSubtitle().getBoundsInRoot().top
//        val watermarkBottom = findWatermark().getBoundsInRoot().bottom
//        assert(subtitleTop < watermarkBottom)
//    }

//    @Test
//    fun watermarkFalse_headerIsDisplayedToEndOfBackButton() {
//        showWatermark = false
//        showHeader = true
//        val subtitleTop = findHeaderSubtitle().getBoundsInRoot().
//        val watermarkBottom = findWatermark().getBoundsInRoot().bottom
//    }

    private fun findBackButton(): SemanticsNodeInteraction {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        return composeTestRule.onNodeWithContentDescription(back)
    }

    private fun findRecordingLabel(): SemanticsNodeInteraction {
        val rec = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec).uppercase()
        return composeTestRule.onNodeWithText(rec)
    }

    private fun findWatermark(): SemanticsNodeInteraction {
        val logo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        return composeTestRule.onNodeWithContentDescription(logo)
    }

    // The title header is a view
    private fun findHeaderTitle(): ViewInteraction = Espresso.onView(withText("title"))

    private fun findHeaderSubtitle(): SemanticsNodeInteraction = composeTestRule.onNodeWithText("subtitle")
}