package com.kaleyra.collaboration_suite_phone_ui.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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