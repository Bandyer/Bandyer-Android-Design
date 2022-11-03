package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomSheetContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var lineState by mutableStateOf<LineState>(LineState.Collapsed(hasBackground = true))

    private var callActions by mutableStateOf(
        ImmutableList(
            listOf(
                CallAction.Microphone(isToggled = false, isEnabled = true) {},
                CallAction.Chat(true) {})
        )
    )

    private
    var isLineClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            BottomSheetContent(
                actions = callActions,
                lineState = lineState,
                onLineClick = { isLineClicked = true },
                itemsPerRow = 4,
                orientation = MutableStateFlow(0)
            )
        }
    }

    @Test
    fun lineStateCollapsed_lineIsCollapsed() {
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true)
            .assertWidthIsEqualTo(CollapsedLineWidth)
    }

    @Test
    fun lineStateExpanded_lineIsExpanded() {
        lineState = LineState.Expanded
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true)
            .assertWidthIsEqualTo(ExpandedLineWidth)
    }

    @Test
    fun userClicksLine_sheetHalfExpand() {
        composeTestRule.onRoot().onChildren().onFirst().performClick()
        composeTestRule.waitForIdle()
        assert(isLineClicked)
    }
}