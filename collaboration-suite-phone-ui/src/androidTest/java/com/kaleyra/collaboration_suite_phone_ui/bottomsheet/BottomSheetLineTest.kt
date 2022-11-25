package com.kaleyra.collaboration_suite_phone_ui.bottomsheet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomSheetLineTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var clicked = false

    @Test
    fun lineStateCollapsed_lineIsCollapsed() {
        composeTestRule.setUpLineTest(LineState.Collapsed(Color.White))
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
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
        composeTestRule.setUpLineTest(LineState.Collapsed(Color.White))
        composeTestRule.onRoot().performClick()
        assert(clicked)
    }

    private fun ComposeContentTestRule.setUpLineTest(state: LineState) {
        setContent {
            Line(
                state = state,
                onClickLabel = "",
                onClick = { clicked = true }
            )
        }
    }
}