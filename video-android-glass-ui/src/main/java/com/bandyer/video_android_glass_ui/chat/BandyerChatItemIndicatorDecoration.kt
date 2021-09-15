package com.bandyer.video_android_glass_ui.chat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.utils.extensions.darkenColor
import com.bandyer.video_android_phone_ui.extensions.dp2px
import com.google.android.material.color.MaterialColors
import java.util.*
import kotlin.math.abs

/**
 * An incremental line indicator while scrolling in the recycler view
 *
 * @property height Float
 * @constructor
 *
 * @credits David Medenjak
 */
class BandyerChatItemIndicatorDecoration(context: Context) : ItemDecoration() {

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
    private val height = context.dp2px(3f).toFloat()

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
     * Tell if layout is rtl
     */
    private val isRTL = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return
        if(itemCount < 2) return
        val parentWidth = parent.width.toFloat()
        val itemWidth = parentWidth / itemCount

        c.drawInactiveIndicator(parent)

        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) return

        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val start = activeChild.left
        val width = activeChild.width

        c.drawHighlights(parent, itemWidth, activePosition + 1, abs(start / width.toFloat()))
    }

    private fun Canvas.drawInactiveIndicator(parent: View) {
        paint.color = colorInactive
        val y = parent.height - this@BandyerChatItemIndicatorDecoration.height / 2f
        drawLine(0f, y, parent.width.toFloat(), y, paint)
    }

    private fun Canvas.drawHighlights(
        parent: View,
        itemWidth: Float,
        highlightPosition: Int,
        progress: Float
    ) {
        paint.color = colorActive
        val y = parent.height - this@BandyerChatItemIndicatorDecoration.height / 2f
        val parentStart = if (isRTL) parent.right else parent.left
        val highlightWidth = (highlightPosition + progress) * itemWidth
        drawLine(parentStart.toFloat(), y, highlightWidth, y, paint)
    }
}