package com.kaleyra.collaboration_suite_phone_ui.call.streams

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.ceil

@Composable
fun AdaptiveGrid(
    modifier: Modifier = Modifier,
    columns: Int,
    children: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = children
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            "Unbounded size not supported"
        }
        require(columns != 0) {
            "0 columns value not valid"
        }

        val rows = ceil(measurables.size / columns.toFloat()).toInt()

        val itemWidth = constraints.maxWidth / columns
        val itemHeight = if (rows != 0) constraints.maxHeight / rows else 0
        val itemConstraints = constraints.copy(maxWidth = itemWidth, maxHeight = itemHeight)

        val lastRowItemsCount = if (rows != 0) measurables.size - (columns * (rows - 1)) else 0
        val lastRowPadding = (constraints.maxWidth - (lastRowItemsCount * itemWidth)) / 2

        val placeables = measurables.map { measurable ->
            measurable.measure(itemConstraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var xPosition = 0
            var yPosition = 0
            var currentRow = 0

            placeables.forEachIndexed { index, placeable ->
                placeable.place(x = xPosition, y = yPosition)

                if (index % columns == columns - 1) {
                    xPosition = 0
                    yPosition += itemHeight
                    currentRow += 1
                } else {
                    xPosition += itemWidth
                }

                if (xPosition == 0 && currentRow == rows - 1) {
                    xPosition += lastRowPadding
                }
            }
        }
    }
}