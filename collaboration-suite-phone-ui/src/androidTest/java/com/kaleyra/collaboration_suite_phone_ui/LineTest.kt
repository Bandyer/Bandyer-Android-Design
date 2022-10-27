package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LineTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var sheetState by mutableStateOf(BottomSheetState(BottomSheetValue.Collapsed))

    private var clicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Line(
                sheetState = sheetState,
                onClickLabel = "",
                onClick = { clicked = true }
            )
        }
    }

    @Test
    fun sheetCollapsed_lineIsCollapsed() {
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
    }

    @Test
    fun sheetExpanded_lineIsExpanded() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.Expanded)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
    }

    @Test
    fun sheetNotCollapsableAndHalfExpanded_lineIsCollapsed() {
        sheetState = BottomSheetState(initialValue = BottomSheetValue.HalfExpanded, collapsable = false)
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
    }

    @Test
    fun userClicksLine_onClickInvoked() {
        composeTestRule.onRoot().performClick()
        assert(clicked)
    }
}