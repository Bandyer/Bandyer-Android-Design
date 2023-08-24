package com.kaleyra.collaboration_suite_phone_ui.fileshare

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareEmptyContent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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