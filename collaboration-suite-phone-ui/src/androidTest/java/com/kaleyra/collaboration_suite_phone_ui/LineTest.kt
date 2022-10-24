package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CollapsedLineWidth
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ExpandedLineWidth
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Line
import com.kaleyra.collaboration_suite_phone_ui.call.compose.LineTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LineTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var clicked = false

    private var collapsed by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Line(
                collapsed = collapsed,
                color = Color.Black,
                onClickLabel = "clickLabel",
                onClick = { clicked = true }
            )
        }
    }

    @Test
    fun collapsedFalse_lineWidthIsCollapsedLineWidth() {
        collapsed = false
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(ExpandedLineWidth)
    }

    @Test
    fun collapsedTrue_lineWidthIsExpandedLineWidth() {
        collapsed = true
        composeTestRule.onNodeWithTag(LineTag, useUnmergedTree = true).assertWidthIsEqualTo(CollapsedLineWidth)
    }

    @Test
    fun userClicksLine_onClickInvoked() {
        composeTestRule.onRoot().performClick()
        assert(clicked)
    }
}