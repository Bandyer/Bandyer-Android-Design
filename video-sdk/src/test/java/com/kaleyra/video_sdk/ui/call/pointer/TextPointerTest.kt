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

package com.kaleyra.video_sdk.ui.call.pointer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.call.pointer.view.TextPointer
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TextPointerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var textWidth = 0

    @Before
    fun setUp() {
        composeTestRule.setContent {
            TextPointer(username = "username", onTextWidth = { textWidth = it })
        }
    }

    @Test
    fun usernameIsDisplayed() {
        composeTestRule.onNodeWithText("username").assertIsDisplayed()
    }

    @Test
    fun textWidthIsInvoked() {
        assertNotEquals(0, textWidth)
    }
}