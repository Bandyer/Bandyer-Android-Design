package com.kaleyra.video_sdk.call.fileshare

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import import com.kaleyra.video_sdk.Rimport com.kaleyra.collaboration_suite_phone_ui.call.fileshare.view.FileShareAppBar
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareAppBar(onBackPressed = { isBackPressed = true })
        }
    }

    @After
    fun tearDown() {
        isBackPressed = false
    }

    @Test
    fun userClicksCollapse_backPressedInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun fileShareTextDisplayed() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }
}