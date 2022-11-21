package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.submenulayout.SubMenuLayout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubMenuLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var closeClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SubMenuLayout(title = "title", onCloseClick = { closeClicked = true }, content = { })
        }
    }

    @Test
    fun titleDisplayed() {
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(closeClicked)
    }
}