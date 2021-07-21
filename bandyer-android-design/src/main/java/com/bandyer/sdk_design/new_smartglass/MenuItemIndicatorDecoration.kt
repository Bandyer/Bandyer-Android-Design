package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.new_smartglass.utils.extensions.darkenColor
import com.google.android.material.color.MaterialColors
import kotlin.math.abs

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
        c.drawInactiveIndicator(parent, y)
        // find active page (which should be highlighted)
        val activeChild = snapHelper.findSnapView(parent.layoutManager) ?: return
        val textView = activeChild.findViewById<View>(R.id.itemText)
        c.drawHighlights(activeChild, textView, y)
    }

    private fun Canvas.drawInactiveIndicator(parent: View, y: Float) {
        paint.color = colorInactive
        drawLine(0f, y, parent.width.toFloat(), y, paint)
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