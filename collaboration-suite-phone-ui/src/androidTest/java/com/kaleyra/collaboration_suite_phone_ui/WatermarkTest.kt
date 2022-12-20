package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
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

    private var logoRes by mutableStateOf(com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo)

    private val logoText = "logoText"

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Watermark(
                logo = painterResource(id = logoRes),
                text = logoText
            )
        }
    }

    @Test
    fun logoIsDisplayed() {
        composeTestRule.onNodeWithContentDescription(findLogo()).assertIsDisplayed()
    }

    @Test
    fun textIsDisplayed() {
        composeTestRule.onNodeWithText(logoText).assertIsDisplayed()
    }

    @Test
    fun testLogoExpandsToMaxWidth() {
        logoRes = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo_clipped
        composeTestRule.onNodeWithContentDescription(findLogo()).assertWidthIsEqualTo(300.dp)
    }

    @Test
    fun testLogoExpandsToMaxHeight() {
        logoRes = com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo
        composeTestRule.onNodeWithContentDescription(findLogo()).assertHeightIsEqualTo(80.dp)
    }

    private fun findLogo() = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
}