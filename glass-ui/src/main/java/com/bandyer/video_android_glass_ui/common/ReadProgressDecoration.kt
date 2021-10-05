package com.bandyer.video_android_glass_ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bandyer.video_android_core_ui.extensions.ColorIntExtensions.darkenColor
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.dp2px
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.getScreenSize
import com.bandyer.video_android_glass_ui.R
import com.google.android.material.color.MaterialColors
import java.util.*


/**
 * An incremental line indicator while scrolling in the recycler view
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
     * The screen width in pixel
     */
    private val screenWidth = context.getScreenSize().x.toFloat()

    /**
     * True if layout is rtl
     */
    private val isRTL = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return
        if(itemCount < 2) return
        val itemWidth = screenWidth / itemCount

        c.drawInactiveIndicator(parent)

        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findLastVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) return

        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val visiblePortion = if(isRTL) activeChild.right.toFloat() else (screenWidth - activeChild.left)
        val progress = visiblePortion / activeChild.width

        c.drawHighlights(parent, itemWidth, activePosition, progress)
    }

    private fun Canvas.drawInactiveIndicator(parent: View) {
        paint.color = colorInactive
        val y = parent.height - this@ReadProgressDecoration.height / 2f
        drawLine(0f, y, parent.width.toFloat(), y, paint)
    }

    private fun Canvas.drawHighlights(
        parent: View,
        itemWidth: Float,
        highlightPosition: Int,
        progress: Float
    ) {
        paint.color = colorActive
        val y = parent.height - this@ReadProgressDecoration.height / 2f
        val parentStart = if (isRTL) parent.right else parent.left
        val highlightWidth = (highlightPosition + progress) * itemWidth
        val stopX = if(isRTL) screenWidth - highlightWidth else highlightWidth
        drawLine(parentStart.toFloat(), y, stopX, y, paint)
    }
}