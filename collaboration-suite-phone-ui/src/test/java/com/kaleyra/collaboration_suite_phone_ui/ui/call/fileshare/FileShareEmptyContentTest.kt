package com.kaleyra.collaboration_suite_phone_ui.ui.call.fileshare

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareEmptyContent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(instrumentedPackages = ["androidx.loader.content"])
@RunWith(RobolectricTestRunner::class)
class FileShareEmptyContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareEmptyContent()
        }
    }

    @Test
    fun titleDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_no_file_shared)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun subtitleDisplayed() {
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_click_to_share_file)
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }
}