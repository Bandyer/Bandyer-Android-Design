package com.kaleyra.video_sdk.ui.call.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardAppBar
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
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