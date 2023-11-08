package com.kaleyra.video_sdk.ui.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.snackbar.CameraRestrictionSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CameraRestrictionSnackbarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testCameraRestrictionSnackbar() {
        composeTestRule.setContent { CameraRestrictionSnackbar() }
        val text = composeTestRule.activity.resources.getString(R.string.kaleyra_user_has_no_video_permissions)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

}