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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.IntSize
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.call.pointer.view.MovablePointer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MovablePointerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pointer = PointerUi(username = "username", 30f, 45f)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            var size by remember { mutableStateOf(IntSize(0, 0)) }
            Box(
                Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { size = it.size }
            ) {
                MovablePointer(pointer, size, floatArrayOf(.5f, .5f))
            }
        }
    }

    @Test
    fun usernameIsDisplayed() {
        composeTestRule.onNodeWithText(pointer.username).assertIsDisplayed()
    }

}