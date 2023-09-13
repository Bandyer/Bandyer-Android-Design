package com.kaleyra.collaboration_suite_phone_ui.ui.call.feedback

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.feedback.view.FeedbackForm
import com.kaleyra.collaboration_suite_phone_ui.call.feedback.view.StarSliderTag
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FeedbackFormTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isDismissed = false

    private var rating = -1f

    private var comment = ""

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FeedbackForm(
                onUserFeedback = { rating, comment ->
                    this@FeedbackFormTest.rating = rating
                    this@FeedbackFormTest.comment = comment
                },
                onDismiss = { isDismissed = true }
            )
        }
    }

    @After
    fun tearDown() {
        isDismissed = false
        rating = -1f
        comment = ""
    }

    @Test
    fun titleIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_evaluate_call)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun closeButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onDismissInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(text).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        assertEquals(true, isDismissed)
    }

    @Test
    fun testSliderDefaultSettings() {
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(5f, 1f.rangeTo(5f), 3))).assertIsDisplayed()
    }

    @Test
    fun oneStar_awfulTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_bad)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            swipeLeft(right, left)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(1f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun twoStars_poorTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_poor)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            val step = width / 4f
            swipeLeft(right, right - step * 3)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(2f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun threeStars_neutralTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_neutral)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            val step = width / 4f
            swipeLeft(right, right - step * 2)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(3f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fourStars_goodTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_good)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            val step = width / 4f
            swipeLeft(right, right - step)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(4f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fiveStars_excellentTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_excellent)
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(5f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun textFieldHasFocus_titleIsNotDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_evaluate_call)
        val textField = composeTestRule.onNode(hasSetTextAction())
        textField.assertIsDisplayed()
        textField.performClick()
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun textFieldHasFocus_heightIncrease() {
        val textField = composeTestRule.onNode(hasSetTextAction())
        val before = textField.getBoundsInRoot().height
        textField.performClick()
        textField.assertHeightIsAtLeast(before + 1.dp)
    }

    @Test
    fun voteButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_vote)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksVote_onUserFeedbackInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_vote)
        val button = composeTestRule.onNodeWithText(text)
        val textField = composeTestRule.onNode(hasSetTextAction())
        textField.performTextInput("text")
        button.assertHasClickAction()
        button.performClick()
        assertEquals(5f, rating)
        assertEquals("text", comment)
    }
}