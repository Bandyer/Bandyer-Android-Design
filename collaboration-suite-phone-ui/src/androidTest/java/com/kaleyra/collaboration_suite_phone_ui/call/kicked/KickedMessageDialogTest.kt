package com.kaleyra.collaboration_suite_phone_ui.call.kicked

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.component.kicked.KickedMessageDialog
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KickedMessageDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var adminName by mutableStateOf("")

    private var isDismissed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            KickedMessageDialog(adminName = adminName, onDismiss = { isDismissed = true })
        }
    }

    @After
    fun tearDown() {
        isDismissed = false
    }

    @Test
    fun emptyAdminName_defaultTextIsDisplayed() {
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_removed, 0, "")
        adminName = ""
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun customAdminName_customTextIsDisplayed() {
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_call_participant_removed, 1, "CustomAdmin")
        adminName = "CustomAdmin"
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}