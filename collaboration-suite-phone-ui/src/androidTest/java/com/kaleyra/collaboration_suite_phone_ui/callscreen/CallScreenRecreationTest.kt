package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.LocalBackPressedDispatcher
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.FileShareComponentTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenRecreationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onRecreation_stateIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            val onBackPressedDispatcher = composeTestRule.activity.onBackPressedDispatcher
            CompositionLocalProvider(LocalBackPressedDispatcher provides onBackPressedDispatcher) {
                CallScreen()
            }
        }

        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        composeTestRule.onNodeWithContentDescription(fileShare).performClick()

        restorationTester.emulateSavedInstanceStateRestore()

        val fileShareAppBarTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShareAppBarTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FileShareComponentTag).assertIsDisplayed()
    }
}