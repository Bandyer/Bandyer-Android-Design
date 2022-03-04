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

package com.kaleyra.collaboration_suite_glass_ui.common.item_decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.extensions.ColorIntExtensions.darkenColor
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.dp2px
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.isRTL
import com.kaleyra.collaboration_suite_glass_ui.R
import com.google.android.material.color.MaterialColors
import java.lang.ref.WeakReference

/**
 * A line item indicator
 *
 * @property snapHelper The [LinearSnapHelper] associated to the recycler view
 * @constructor
 */
internal class MenuProgressIndicator(
    context: Context,
    snapHelper: LinearSnapHelper
) : RecyclerView.ItemDecoration() {

    /**
     * SnapHelper reference
     */
    private val snapHelperWeakReference = WeakReference(snapHelper)

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

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom += height.toInt()
    }

    /**
     * Tell if layout is rtl
     */
    private val isRTL = context.isRTL()

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val y = parent.height - this@MenuProgressIndicator.height / 2f

        val layoutManager = parent.layoutManager as LinearLayoutManager
        val firstPos = layoutManager.findFirstVisibleItemPosition()
        val lastPos = layoutManager.findLastVisibleItemPosition()
        if (firstPos == RecyclerView.NO_POSITION || lastPos == RecyclerView.NO_POSITION) return
        val first = layoutManager.findViewByPosition(firstPos) ?: return
        val last = layoutManager.findViewByPosition(lastPos) ?: return
        c.drawInactiveIndicator(parent, first, last, firstPos, lastPos, y)

        // find active page (which should be highlighted)
        val activeChild = snapHelperWeakReference.get()?.findSnapView(layoutManager) ?: return
        val textView = activeChild.findViewById<View>(R.id.bandyer_text)
        c.drawHighlights(activeChild, textView, y)
    }

    private fun Canvas.drawInactiveIndicator(
        parent: RecyclerView,
        first: View,
        last: View,
        firstPos: Int,
        lastPos: Int,
        y: Float
    ) {
        paint.color = colorInactive
        val itemCount = parent.layoutManager!!.itemCount
        val firstTextView = first.findViewById<View>(R.id.bandyer_text)
        val lastTextView = last.findViewById<View>(R.id.bandyer_text)

        var startX = if (!isRTL) parent.left else parent.right
        var endX = if (!isRTL) parent.right else parent.left
        if (firstPos == 0) startX = first.left + firstTextView.left + if (!isRTL) 0 else lastTextView.width
        if (lastPos == itemCount - 1) endX = last.left + lastTextView.left + if (!isRTL) lastTextView.width else 0
        drawLine(startX.toFloat(), y, endX.toFloat(), y, paint)
    }

    private fun Canvas.drawHighlights(
        activeChild: View,
        textView: View,
        y: Float
    ) {
        paint.color = colorActive
        val textStart = activeChild.left.toFloat() + textView.left
        drawLine(textStart, y, textStart + textView.width, y, paint)
    }
}