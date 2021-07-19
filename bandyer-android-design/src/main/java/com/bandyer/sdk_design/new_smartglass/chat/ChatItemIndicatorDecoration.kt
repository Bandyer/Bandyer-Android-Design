package com.bandyer.sdk_design.new_smartglass.chat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.new_smartglass.utils.extensions.darkenColor
import com.google.android.material.color.MaterialColors
import kotlin.math.truncate

/**
 * LinePagerIndicatorDecoration
 *
 * @property height Float
 * @constructor
 *
 * @credits David Medenjak
 */
class ChatItemIndicatorDecoration(context: Context, private val height: Float) : ItemDecoration() {

    /**
     * Indicator active color
     */
    private val colorActive = MaterialColors.getColor(context, R.attr.colorSecondary, Color.WHITE)

    /**
     * Indicator inactive color
     */
    private val colorInactive = colorActive.darkenColor(0.4f)

    /**
     * Interpolation animation
     */
    private val interpolator: Interpolator = AccelerateDecelerateInterpolator()

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
        val itemCount = parent.adapter?.itemCount ?: return
        val parentWidth = parent.width.toFloat()
        val itemWidth = parentWidth / itemCount

        c.drawInactiveIndicator(parent)

        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) return

        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val left = activeChild.left
        val width = activeChild.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress = interpolator.getInterpolation(left * -1 / width.toFloat())
        c.drawHighlights(parent, itemCount,truncate(itemWidth) + 1f, activePosition, progress)
    }

    private fun Canvas.drawInactiveIndicator(parent: View) {
        paint.color = colorInactive
        val y = parent.height - this@ChatItemIndicatorDecoration.height / 2f
        drawLine(0f, y, parent.width.toFloat(), y, paint)
    }

    private fun Canvas.drawHighlights(
        parent: View,
        itemCount: Int,
        itemWidth: Float,
        highlightPosition: Int,
        progress: Float
    ) {
        paint.color = colorActive
        val y = parent.height - this@ChatItemIndicatorDecoration.height / 2f
        var highlightStart = itemWidth * highlightPosition
        // calculate partial highlight
        val partialHighlight = itemWidth * progress

        // draw the cut off highlight
        drawLine(highlightStart + partialHighlight, y, highlightStart + itemWidth, y, paint)

        // draw the highlight overlapping to the next item as well
        if (highlightPosition < itemCount - 1) {
            highlightStart += itemWidth
            drawLine(highlightStart, y, highlightStart + partialHighlight, y, paint)
        }
    }
}