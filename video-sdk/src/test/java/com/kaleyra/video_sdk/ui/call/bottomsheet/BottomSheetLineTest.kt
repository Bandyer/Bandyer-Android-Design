package com.kaleyra.video_sdk.ui.call.bottomsheet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.kaleyra.video_sdk.call.*
import com.kaleyra.video_sdk.call.bottomsheet.CollapsedLineWidth
import com.kaleyra.video_sdk.call.bottomsheet.ExpandedLineWidth
import com.kaleyra.video_sdk.call.bottomsheet.Line
import com.kaleyra.video_sdk.call.bottomsheet.LineState
import com.kaleyra.video_sdk.call.bottomsheet.LineTag
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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