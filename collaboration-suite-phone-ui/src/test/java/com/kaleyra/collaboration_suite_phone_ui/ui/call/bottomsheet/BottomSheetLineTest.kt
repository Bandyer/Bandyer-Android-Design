package com.kaleyra.collaboration_suite_phone_ui.ui.call.bottomsheet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(instrumentedPackages = ["androidx.loader.content"])
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