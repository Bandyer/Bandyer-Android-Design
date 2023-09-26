package com.kaleyra.collaboration_suite_phone_ui.call.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.WhiteboardAppBar
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhiteboardAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isBackPressed = false

    private var isUploadClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardAppBar(
                onBackPressed = { isBackPressed = true },
                onUploadClick = { isUploadClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        isBackPressed = false
        isUploadClicked = false
    }

    @Test
    fun userClicksCollapse_backPressedInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksUpload_uploadClickInvoked() {
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(upload).performClick()
        assert(isUploadClicked)
    }

    @Test
    fun fileShareTextDisplayed() {
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
    }
}