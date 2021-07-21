package com.bandyer.sdk_design.new_smartglass.menu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.new_smartglass.utils.extensions.darkenColor
import com.google.android.material.color.MaterialColors

/**
 * MenuPagerIndicatorDecoration
 *
 * @property height Float
 * @constructor
 *
 */
class MenuItemIndicatorDecoration(
    context: Context,
    private val snapHelper: LinearSnapHelper,
    private val height: Float
) : RecyclerView.ItemDecoration() {

    /**
     * Indicator active color
     */
    private val colorActive = MaterialColors.getColor(context, R.attr.colorSecondary, Color.WHITE)

    /**
     * Indicator inactive color
     */
    private val colorInactive = colorActive.darkenColor(0.4f)

    /**
     * Paint used to draw
     */
    private val paint = Paint().apply {
        strokeCap = Paint.Cap.BUTT
        strokeWidth = height
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val y = parent.height - this@MenuItemIndicatorDecoration.height / 2f

        val layoutManager = parent.layoutManager as LinearLayoutManager
        val firstPos = layoutManager.findFirstVisibleItemPosition()
        val lastPos = layoutManager.findLastVisibleItemPosition()
        if (firstPos == RecyclerView.NO_POSITION || lastPos == RecyclerView.NO_POSITION) return
        val first = layoutManager.findViewByPosition(firstPos) ?: return
        val last = layoutManager.findViewByPosition(lastPos) ?: return
        c.drawInactiveIndicator(parent, first, last, firstPos, lastPos, y)

        // find active page (which should be highlighted)
        val activeChild = snapHelper.findSnapView(layoutManager) ?: return
        val textView = activeChild.findViewById<View>(R.id.itemText)
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
        val firstTextView = first.findViewById<View>(R.id.itemText)
        val lastTextView = last.findViewById<View>(R.id.itemText)

        var startX = parent.left
        var endX = parent.right
        when {
            firstPos == 0 -> startX = first.left + firstTextView.left
            lastPos == itemCount - 1 -> endX = last.left + lastTextView.left + lastTextView.width
        }
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