package com.kaleyra.collaboration_suite_phone_ui.bottomsheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertEquals
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
        composeTestRule.setUpLineTest(LineState.Collapsed(hasBackground = true))
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
        composeTestRule.setUpLineTest(LineState.Collapsed(hasBackground = true))
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