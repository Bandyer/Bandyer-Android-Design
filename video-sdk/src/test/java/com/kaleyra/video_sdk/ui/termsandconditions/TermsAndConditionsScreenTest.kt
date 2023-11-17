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

package com.kaleyra.video_sdk.ui.termsandconditions

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.termsandconditions.model.TermsAndConditionsUiState
import com.kaleyra.video_sdk.termsandconditions.screen.TermsAndConditionsScreen
import com.kaleyra.video_sdk.termsandconditions.screen.TermsProgressIndicatorTag
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class TermsAndConditionsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var accepted = false

    private var declined = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            TermsAndConditionsScreen(
                uiState = TermsAndConditionsUiState(),
                title = "title",
                message = "message",
                acceptText = "accept",
                declineText = "decline",
                onAccept = { accepted = true },
                onDecline = { declined = true }
            )
        }
    }

    @After
    fun tearDown() {
        accepted = false
        declined = false
    }

    @Test
    fun testTitleIsDisplayed() {
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
    }

    @Test
    fun testMessageIsDisplayed() {
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
    }

    @Test
    fun testAcceptButtonIsDisplayed() {
        composeTestRule.onNodeWithText("accept").assertIsDisplayed()
        composeTestRule.onNodeWithText("accept").assertHasClickAction()
    }

    @Test
    fun testDeclineButtonIsDisplayed() {
        composeTestRule.onNodeWithText("decline").assertIsDisplayed()
        composeTestRule.onNodeWithText("decline").assertHasClickAction()
    }

    @Test
    fun loaderShownOnAccept() {
        composeTestRule.onNodeWithText("accept").performClick()
        composeTestRule.onNodeWithTag(TermsProgressIndicatorTag).assertRangeInfoEquals(ProgressBarRangeInfo.Indeterminate)
    }

    @Test
    fun testOnAcceptInvoked() {
        composeTestRule.onNodeWithText("accept").performClick()
        assertEquals(true, accepted)
    }

    @Test
    fun testOnDeclineInvoked() {
        composeTestRule.onNodeWithText("decline").performClick()
        assertEquals(true, declined)
    }

}