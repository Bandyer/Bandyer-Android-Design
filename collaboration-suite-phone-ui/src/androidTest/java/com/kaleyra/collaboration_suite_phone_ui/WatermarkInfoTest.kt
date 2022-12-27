package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Watermark
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatermarkInfoTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var watermarkInfo by mutableStateOf(WatermarkInfo(null, null))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Watermark(watermarkInfo = watermarkInfo)
        }
    }

    @Test
    fun imageNotNull_watermarkImageIsDisplayed() {
        watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo)
        composeTestRule.onNodeWithContentDescription(findLogo()).assertIsDisplayed()
    }

    @Test
    fun textNotNull_textIsDisplayed() {
        watermarkInfo = WatermarkInfo(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun imageNull_textIsDisplayedAtTheStart() {
        watermarkInfo = WatermarkInfo(image = null, text = "text")
        composeTestRule.onNodeWithText("text").assertLeftPositionInRootIsEqualTo(0.dp)
    }

    @Test
    fun imageNotNull_textIsDisplayedToImageEnd() {
        watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo, text = "text")
        val imageWidth = composeTestRule.onNodeWithContentDescription(findLogo()).getBoundsInRoot().width
        val imageSpacerWidth = 16.dp
        val expectedPosition = imageWidth + imageSpacerWidth
        composeTestRule.onNodeWithText("text").assertLeftPositionInRootIsEqualTo(expectedPosition)
    }

    @Test
    fun testLogoExpandsToMaxWidth() {
        watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo_clipped)
        composeTestRule.onNodeWithContentDescription(findLogo()).assertWidthIsEqualTo(300.dp)
    }

    @Test
    fun testLogoExpandsToMaxHeight() {
        watermarkInfo = WatermarkInfo(image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo)
        composeTestRule.onNodeWithContentDescription(findLogo()).assertHeightIsEqualTo(80.dp)
    }

    private fun findLogo() = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
}