package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Watermark
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatermarkTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var image by mutableStateOf<Int?>(null)

    private val text = "text"

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Watermark(
                image = image?.let { painterResource(id = it) },
                text = text
            )
        }
    }

    @Test
    fun imageNotNull_watermarkImageIsDisplayed() {
        image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo
        composeTestRule.onNodeWithContentDescription(findLogo()).assertIsDisplayed()
    }

    @Test
    fun textNotNull_textIsDisplayed() {
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun imageNull_textIsDisplayedAtTheStart() {
        image = null
        val padding = 16.dp
        composeTestRule.onNodeWithText(text).assertLeftPositionInRootIsEqualTo(padding)
    }

    @Test
    fun imageNotNull_textIsDisplayedToImageEnd() {
        image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo
        val padding = 16.dp
        val imageWidth = composeTestRule.onNodeWithContentDescription(findLogo()).getBoundsInRoot().width
        val imageSpacerWidth = 16.dp
        val expectedPosition = padding + imageWidth + imageSpacerWidth
        composeTestRule.onNodeWithText(text).assertLeftPositionInRootIsEqualTo(expectedPosition)
    }

    @Test
    fun testLogoExpandsToMaxWidth() {
        image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo_clipped
        composeTestRule.onNodeWithContentDescription(findLogo()).assertWidthIsEqualTo(300.dp)
    }

    @Test
    fun testLogoExpandsToMaxHeight() {
        image = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo
        composeTestRule.onNodeWithContentDescription(findLogo()).assertHeightIsEqualTo(80.dp)
    }

    private fun findLogo() = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
}