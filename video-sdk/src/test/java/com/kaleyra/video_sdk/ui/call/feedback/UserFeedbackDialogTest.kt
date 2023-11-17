/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.ui.call.feedback

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.feedback.UserFeedbackDialog
import com.kaleyra.video_sdk.call.feedback.view.FeedbackFormTag
import com.kaleyra.video_sdk.call.feedback.view.FeedbackSentTag
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserFeedbackDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isDismissed = false

    private var rating = -1f

    private var comment = ""

    @Before
    fun setUp() {
        composeTestRule.setContent {
            UserFeedbackDialog(
                onUserFeedback = { rating, comment ->
                    this@UserFeedbackDialogTest.rating = rating
                    this@UserFeedbackDialogTest.comment = comment
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
    fun feedbackFormIsDisplayed() {
        composeTestRule.onNodeWithTag(FeedbackFormTag).assertIsDisplayed()
    }

    @Test
    fun userClicksVote_feedbackSentIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_vote)
        composeTestRule.onNodeWithText(text).performClick()
        composeTestRule.onNodeWithTag(FeedbackSentTag).assertIsDisplayed()
    }

    @Test
    fun userClicksVote_onUserFeedbackInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_vote)
        val button = composeTestRule.onNodeWithText(text)
        val textField = composeTestRule.onNode(hasSetTextAction())
        textField.performTextInput("text")
        button.performClick()
        TestCase.assertEquals(5f, rating)
        TestCase.assertEquals("text", comment)
    }

    @Test
    fun userDismissesDialog_onDismissInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(text).performClick()
        TestCase.assertEquals(true, isDismissed)
    }
}