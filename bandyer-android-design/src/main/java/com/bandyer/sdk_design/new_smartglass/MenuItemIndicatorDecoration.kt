package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
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
class MenuItemIndicatorDecoration(context: Context, private val snapHelper: LinearSnapHelper, private val height: Float) : RecyclerView.ItemDecoration() {

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

        c.drawInactiveIndicator(parent)

        // find active page (which should be highlighted)
        val activeChild = snapHelper.findSnapView(parent.layoutManager) ?: return
        parent.getChildAdapterPosition(activeChild)

        c.drawHighlights(parent, activeChild, activeChild.findViewById(R.id.itemText))
    }

    private fun Canvas.drawInactiveIndicator(parent: View) {
        paint.color = colorInactive
        val y = parent.height - this@MenuItemIndicatorDecoration.height / 2f
        drawLine(0f, y, parent.width.toFloat(), y, paint)
    }

    private fun Canvas.drawHighlights(
        parent: View,
        activeChild: View,
        textView: View
    ) {
        paint.color = colorActive

        val y = parent.height - this@MenuItemIndicatorDecoration.height / 2f

        drawLine(activeChild.left.toFloat() + textView.left, y, activeChild.left.toFloat() + textView.left + textView.width, y, paint)
    }
}