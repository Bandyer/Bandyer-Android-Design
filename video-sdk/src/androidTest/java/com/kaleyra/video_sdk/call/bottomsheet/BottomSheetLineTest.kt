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

package com.kaleyra.video_sdk.call.bottomsheet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomSheetLineTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var isLineClicked = false

    @After
    fun tearDown() {
        isLineClicked = false
    }

    @Test
    fun lineStateCollapsed_lineIsCollapsed() {
        composeTestRule.setUpLineTest(LineState.Collapsed(Color.White.toArgb()))
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(
            CollapsedLineWidth
        )
    }

    @Test
    fun lineStateExpanded_lineIsExpanded() {
        composeTestRule.setUpLineTest(LineState.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(
            ExpandedLineWidth
        )
    }

    @Test
    fun userClicksLine_onClickInvoked() {
        composeTestRule.setUpLineTest(LineState.Collapsed(Color.White.toArgb()))
        composeTestRule.onRoot().performClick()
        assert(isLineClicked)
    }

    private fun ComposeContentTestRule.setUpLineTest(state: LineState) {
        setContent {
            Line(
                state = state,
                onClickLabel = "",
                onClick = { isLineClicked = true }
            )
        }
    }
}