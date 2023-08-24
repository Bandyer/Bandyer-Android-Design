package com.kaleyra.collaboration_suite_phone_ui.fileshare

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.MaxFileSizeDialog
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaxFileSizeDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    var isDismissed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
           MaxFileSizeDialog {
               isDismissed = true
           }
        }
    }

    @After
    fun tearDown() {
        isDismissed = false
    }

    @Test
    fun fileShareLimitTitleIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_max_bytes_dialog_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareLimitDescriptionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_max_bytes_dialog_descr)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksOnOk_dialogIsDismissed() {
        val ok = composeTestRule.activity.getString(R.string.kaleyra_button_ok)
        composeTestRule.onNodeWithText(ok).performClick()
        assert(isDismissed)
    }
}