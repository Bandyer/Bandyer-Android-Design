package com.kaleyra.collaboration_suite_phone_ui.ui.call.subfeaturelayout

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.subfeaturelayout.SubFeatureLayout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
class SubFeatureLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isCloseClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SubFeatureLayout(title = "title", onCloseClick = { isCloseClicked = true }, content = { })
        }
    }

    @After
    fun tearDown() {
        isCloseClicked = false
    }

    @Test
    fun titleDisplayed() {
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isCloseClicked)
    }
}