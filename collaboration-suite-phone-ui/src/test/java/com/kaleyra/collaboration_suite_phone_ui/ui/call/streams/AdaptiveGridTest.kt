package com.kaleyra.collaboration_suite_phone_ui.ui.call.streams

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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.AdaptiveGrid
import com.kaleyra.collaboration_suite_phone_ui.ui.assertRightPositionInRootIsEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.ceil
import kotlin.math.roundToInt


@RunWith(RobolectricTestRunner::class)
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
            composeTestRule.onNodeWithTag("child$index").getUnclippedBoundsInRoot().width.assertIsEqualTo(itemWidth, "width", Dp(1f))
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
            composeTestRule.onNodeWithTag("child$index").getUnclippedBoundsInRoot().height.assertIsEqualTo(itemHeight, "height", Dp(1f))
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
        val roundedItemWidth = Dp(itemWidth.value.toInt().toFloat())
        var column = 0
        repeat(children - lastRowItemsCount) { index ->
            composeTestRule.onNodeWithTag("child$index").getUnclippedBoundsInRoot().left.assertIsEqualTo(roundedItemWidth * column, "left", Dp(1f))
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
        composeTestRule.onNodeWithTag("child${lastRowFirstItemIndex}").getUnclippedBoundsInRoot().left.assertIsEqualTo(lastRowPadding, "left", Dp(1f))
        repeat(lastRowItemsCount - 1) { index ->
            composeTestRule.onNodeWithTag("child${lastRowFirstItemIndex + index + 1}").getUnclippedBoundsInRoot().left.assertIsEqualTo(lastRowPadding + itemWidth * (index + 1), "left", Dp(1f))
        }
        composeTestRule.onNodeWithTag("child${children - 1}").getUnclippedBoundsInRoot().right.assertIsEqualTo(parentWidth - lastRowPadding, "right", Dp(1f))
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