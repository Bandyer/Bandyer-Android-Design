@file:OptIn(ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet.*
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LineTest {

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
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
    }

    @Test
    fun userClicksLine_onClickInvoked() {
        composeTestRule.setUpLineTest(LineState.Collapsed(hasBackground = true))
        composeTestRule.onRoot().performClick()
        assert(clicked)
    }

    @Test
    fun sheetCollapsing_mapToLineState_lineCollapsedWithBackground() {
        val sheetState = spyk(BottomSheetState(BottomSheetValue.Collapsed))
        var lineState by mutableStateOf<LineState>(LineState.Expanded)
        every { sheetState.targetValue } returns BottomSheetValue.Collapsed
        every { sheetState.progress.fraction } returns 0.8f
        composeTestRule.setContent {
            lineState = mapToLineState(sheetState = sheetState)
        }
        assertEquals(LineState.Collapsed(hasBackground = true), lineState)
    }

    @Test
    fun sheetCollapsed_mapToLineState_lineCollapsedWithNoBackground() {
        val sheetState = spyk(BottomSheetState(BottomSheetValue.Collapsed))
        var lineState by mutableStateOf<LineState>(LineState.Expanded)
        every { sheetState.targetValue } returns BottomSheetValue.Collapsed
        every { sheetState.progress.fraction } returns 1f
        composeTestRule.setContent {
            lineState = mapToLineState(sheetState = sheetState)
        }
        assertEquals(LineState.Collapsed(hasBackground = false), lineState)
    }

    @Test
    fun sheetHalfExpandingAndNotCollapsable_mapToLineState_lineCollapsedWithBackground() {
        val sheetState = spyk(BottomSheetState(BottomSheetValue.Expanded, collapsable = false))
        var lineState by mutableStateOf<LineState>(LineState.Expanded)
        every { sheetState.targetValue } returns BottomSheetValue.HalfExpanded
        composeTestRule.setContent {
            lineState = mapToLineState(sheetState = sheetState)
        }
        assertEquals(LineState.Collapsed(hasBackground = true), lineState)
    }

    @Test
    fun sheetHalfExpanding_mapToLineState_lineExpanded() {
        val sheetState = spyk(BottomSheetState(BottomSheetValue.Collapsed))
        var lineState by mutableStateOf<LineState>(LineState.Collapsed(hasBackground = false))
        every { sheetState.targetValue } returns BottomSheetValue.HalfExpanded
        every { sheetState.progress.fraction } returns 0.8f
        composeTestRule.setContent {
            lineState = mapToLineState(sheetState = sheetState)
        }
        assertEquals(LineState.Expanded, lineState)
    }

    @Test
    fun sheetHalfExpanded_mapToLineState_lineExpanded() {
        val sheetState = spyk(BottomSheetState(BottomSheetValue.Collapsed))
        var lineState by mutableStateOf<LineState>(LineState.Collapsed(hasBackground = false))
        every { sheetState.targetValue } returns BottomSheetValue.HalfExpanded
        every { sheetState.progress.fraction } returns 1f
        composeTestRule.setContent {
            lineState = mapToLineState(sheetState = sheetState)
        }
        assertEquals(LineState.Expanded, lineState)
    }

    @Test
    fun sheetExpanding_mapToLineState_lineExpanded() {
        val sheetState = spyk(BottomSheetState(BottomSheetValue.Collapsed))
        var lineState by mutableStateOf<LineState>(LineState.Collapsed(hasBackground = false))
        every { sheetState.targetValue } returns BottomSheetValue.Expanded
        every { sheetState.progress.fraction } returns 0.8f
        composeTestRule.setContent {
            lineState = mapToLineState(sheetState = sheetState)
        }
        assertEquals(LineState.Expanded, lineState)
    }

    @Test
    fun sheetExpanded_mapToLineState_lineExpanded() {
        val sheetState = spyk(BottomSheetState(BottomSheetValue.Collapsed))
        var lineState by mutableStateOf<LineState>(LineState.Collapsed(hasBackground = false))
        every { sheetState.targetValue } returns BottomSheetValue.Expanded
        every { sheetState.progress.fraction } returns 1f
        composeTestRule.setContent {
            lineState = mapToLineState(sheetState = sheetState)
        }
        assertEquals(LineState.Expanded, lineState)
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