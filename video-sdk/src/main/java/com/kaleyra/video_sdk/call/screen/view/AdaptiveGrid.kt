/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.screen.view

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