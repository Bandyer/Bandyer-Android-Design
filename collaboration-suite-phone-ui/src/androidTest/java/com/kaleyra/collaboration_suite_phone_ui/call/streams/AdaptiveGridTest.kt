package com.kaleyra.collaboration_suite_phone_ui.call.streams

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.assertRightPositionInRootIsEqualTo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.AdaptiveGrid
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.ceil

@RunWith(AndroidJUnit4::class)
class AdaptiveGridTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

//    Tests do not catch exceptions (tried with both @Test(expected=..) and assertThrows

//    @Test(expected = IllegalStateException::class)
//    fun unboundedSize_illegalStateExceptionThrown() {
//        composeTestRule.setContent {
//            AdaptiveGrid(
//                columns = 5,
//                modifier = Modifier.wrapContentSize(unbounded = true),
//                children = { }
//            )
//        }
//    }

//    @Test(expected = IllegalArgumentException::class)
//    fun zeroColumns_illegalArgumentExceptionThrown() {
//        composeTestRule.setContent {
//            AdaptiveGrid(columns = 0, children = { })
//        }
//    }

    @Test
    fun noItems_layoutDoesNotThrowException() {
        composeTestRule.setContent {
            AdaptiveGrid(columns = 3, children = { })
        }
    }

    @Test
    fun testItemsWidth() {
        val columns = 3
        val children = 25
        composeTestRule.setAdaptiveGridContent(columns, children)
        val itemWidth = composeTestRule.onNodeWithTag("adaptiveGrid").getBoundsInRoot().width / columns
        repeat(children) { index ->
            composeTestRule.onNodeWithTag("child$index").assertWidthIsEqualTo(itemWidth)
        }
    }

    @Test
    fun testItemsHeight() {
        val columns = 5
        val children = 12
        val rows = ceil(children / columns.toFloat()).toInt()
        composeTestRule.setAdaptiveGridContent(columns, children)
        val itemHeight = composeTestRule.onNodeWithTag("adaptiveGrid").getBoundsInRoot().height / rows
        repeat(children) { index ->
            composeTestRule.onNodeWithTag("child$index").assertHeightIsEqualTo(itemHeight)
        }
    }

    @Test
    fun testAllRowsExceptLastOneItemsBounds() {
        val columns = 3
        val children = 5
        val rows = ceil(children / columns.toFloat()).toInt()
        val lastRowItemsCount = children - (columns * (rows - 1))
        composeTestRule.setAdaptiveGridContent(columns, children)
        val itemWidth = composeTestRule.onNodeWithTag("adaptiveGrid").getBoundsInRoot().width / columns
        var column = 0
        repeat(children - lastRowItemsCount) { index ->
            composeTestRule.onNodeWithTag("child$index").assertLeftPositionInRootIsEqualTo(itemWidth * column)
            column = if (index % columns == columns - 1) 0 else column + 1
        }
    }

    @Test
    fun testLastRowWithOneItemItemsBounds() {
        testLastRowItemsBounds(columns = 2, children = 3)
    }

    @Test
    fun testFullLastRowItemsBounds() {
        testLastRowItemsBounds(columns = 3, children = 6)
    }

    @Test
    fun testGenericLastRowItemsBounds() {
        testLastRowItemsBounds(columns = 6, children = 21)
    }

    private fun testLastRowItemsBounds(columns: Int, children: Int) {
        val rows = ceil(children / columns.toFloat()).toInt()
        val lastRowItemsCount = children - (columns * (rows - 1))
        composeTestRule.setAdaptiveGridContent(columns, children)
        val parentWidth = composeTestRule.onNodeWithTag("adaptiveGrid").getBoundsInRoot().width
        val itemWidth = parentWidth / columns
        val lastRowPadding = (parentWidth - (lastRowItemsCount * itemWidth)) / 2
        val lastRowFirstItemIndex = children - lastRowItemsCount
        composeTestRule.onNodeWithTag("child${lastRowFirstItemIndex}").assertLeftPositionInRootIsEqualTo(lastRowPadding)
        repeat(lastRowItemsCount - 1) { index ->
            composeTestRule.onNodeWithTag("child${lastRowFirstItemIndex + index + 1}").assertLeftPositionInRootIsEqualTo(lastRowPadding + itemWidth * (index + 1))
        }
        composeTestRule.onNodeWithTag("child${children - 1}").assertRightPositionInRootIsEqualTo(parentWidth - lastRowPadding)
    }

    private fun ComposeContentTestRule.setAdaptiveGridContent(columns: Int, children: Int) {
        setContent {
            AdaptiveGrid(
                columns = columns,
                children = {
                    repeat(children) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .semantics { testTag = "child$it" })
                    }
                },
                modifier = Modifier.semantics { testTag = "adaptiveGrid" }
            )
        }
    }
}