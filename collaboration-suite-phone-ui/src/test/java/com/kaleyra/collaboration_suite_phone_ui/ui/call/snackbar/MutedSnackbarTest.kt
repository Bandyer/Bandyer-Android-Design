package com.kaleyra.collaboration_suite_phone_ui.ui.call.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.snackbar.MutedSnackbar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class MutedSnackbarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testMutedSnackbar() {
        composeTestRule.setContent { MutedSnackbar() }
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_muted_by_admin, 0, "")
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testMutedSnackbarWithAdminName() {
        val adminName = "adminUsername"
        composeTestRule.setContent { MutedSnackbar(adminName) }
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_muted_by_admin, 1, adminName)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}