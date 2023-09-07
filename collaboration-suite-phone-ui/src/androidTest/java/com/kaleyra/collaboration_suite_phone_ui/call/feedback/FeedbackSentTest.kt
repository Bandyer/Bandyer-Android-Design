package com.kaleyra.collaboration_suite_phone_ui.call.feedback

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.feedback.FeedbackSent
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackSentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isDismissed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FeedbackSent(onDismiss = { isDismissed = true })
        }
    }

    @After
    fun tearDown() {
        isDismissed = false
    }

    @Test
    fun titleIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_thank_you)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun subtitleIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_see_you_soon)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun closeButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_close)
        val button = composeTestRule.onNodeWithText(text)
        button.assertIsDisplayed()
        button.assertHasClickAction()
        button.performClick()
        assertEquals(true, isDismissed)
    }
}