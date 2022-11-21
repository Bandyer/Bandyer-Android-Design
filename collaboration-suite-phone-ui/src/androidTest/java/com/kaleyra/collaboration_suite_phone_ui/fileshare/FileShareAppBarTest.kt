package com.kaleyra.collaboration_suite_phone_ui.fileshare

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareAppBar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var backPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareAppBar(onBackPressed = { backPressed = true })
        }
    }

    @Test
    fun userClicksCollapse_backPressedInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(backPressed)
    }

    @Test
    fun fileShareTextDisplayed() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }
}