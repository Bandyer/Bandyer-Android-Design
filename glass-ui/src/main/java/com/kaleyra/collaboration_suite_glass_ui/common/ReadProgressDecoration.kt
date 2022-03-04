/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.kaleyra.collaboration_suite_core_ui.extensions.ColorIntExtensions.darkenColor
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.dp2px
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.isRTL
import com.kaleyra.collaboration_suite_glass_ui.R
import com.google.android.material.color.MaterialColors

/**
 * A full screen incremental line indicator while scrolling in the recycler view.
 * Use android:clipChildren="false" if the recycler view is not full screen.
 *
 * @property height Float
 * @constructor
 */
internal class ReadProgressDecoration(context: Context) : ItemDecoration() {

    /**
     * Indicator active color
     */
    private val colorActive = MaterialColors.getColor(context, R.attr.colorSecondary, Color.WHITE)

    /**
     * Indicator inactive color
     */
    private val colorInactive = colorActive.darkenColor(0.4f)

    /**
     * Indicator height
     */
    private val height = context.dp2px(4f).toFloat()

    /**
     * Paint used to draw
     */
    private val paint = Paint().apply {
        strokeCap = Paint.Cap.BUTT
        strokeWidth = height
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    /**
     * True if layout is rtl
     */
    private val isRTL = context.isRTL()

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return
        if (itemCount < 2) return

        // Set the decoration at the bottom of the recyclerView's parent
        val y = (parent.parent as View).height - parent.top - this@ReadProgressDecoration.height / 2f
        val containerWidth = (parent.parent as View).width

        canvas.drawInactive(parent, y, containerWidth)
        canvas.drawHighlights(parent, y, containerWidth)
    }

    private fun Canvas.drawInactive(parent: RecyclerView, y: Float, containerWidth: Int) {
        val startX = -if (isRTL) parent.right else parent.left
        drawLine(startX.toFloat(), y, containerWidth.toFloat(), y, paint.apply { color = colorInactive })
    }

    private fun Canvas.drawHighlights(parent: RecyclerView, y: Float, containerWidth: Int) {
        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findLastVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) return
        val activeChild = layoutManager.findViewByPosition(activePosition)!!

        // Compute the visible portion of the recyclerView's last visible item in pixel
        val visiblePortion = if (isRTL) activeChild.right else containerWidth - activeChild.left
        // Then compute the percentage of the visible part
        val progress = visiblePortion.toFloat() / activeChild.width

        // Compute the highlight width
        val itemWidth = containerWidth / layoutManager.itemCount
        val highlightWidth = (activePosition + progress) * itemWidth

        val startX = if(isRTL) containerWidth.toFloat() else -parent.left.toFloat()
        val stopX = if (isRTL) containerWidth - highlightWidth else highlightWidth

        drawLine(startX, y, stopX , y, paint.apply { color = colorActive })
    }
}