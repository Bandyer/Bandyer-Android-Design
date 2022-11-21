package com.kaleyra.collaboration_suite_phone_ui.fileshare

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareFab
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareFabTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var collapsed by mutableStateOf(false)

    private var clicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareFab(
                collapsed = collapsed,
                onClick = { clicked = true }
            )
        }
    }

    @Test
    fun collapsedTrue_textNotExists() {
        collapsed = true
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        val addDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(addDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(add).assertDoesNotExist()
    }

    @Test
    fun collapsedFalse_textDisplayed() {
        collapsed = false
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        val addDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(addDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(add).assertIsDisplayed()
    }
}