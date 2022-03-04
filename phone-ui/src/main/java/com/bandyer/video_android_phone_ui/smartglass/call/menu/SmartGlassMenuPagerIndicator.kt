/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bandyer.video_android_phone_ui.smartglass.call.menu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bandyer.video_android_phone_ui.R
import com.bandyer.video_android_phone_ui.extensions.getPagerIndicatorBooleanAttribute
import com.bandyer.video_android_phone_ui.extensions.getPagerIndicatorColorAttribute
import com.bandyer.video_android_phone_ui.extensions.getPagerIndicatorDimensionPixelSizeAttribute
import com.bandyer.video_android_phone_ui.extensions.getPagerIndicatorIntAttribute
import com.bandyer.video_android_phone_ui.extensions.isRtl
import kotlin.math.abs

/**
 * Represents a customizable pager indicator for view pagers and recycler views
 * @constructor
 */
class SmartGlassMenuPagerIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle), ViewPager.OnPageChangeListener {

    private companion object {
        private const val DEFAULT_DOT_COUNT = 5
        private const val DEFAULT_FADING_DOT_COUNT = 1
        private const val DEFAULT_DOT_RADIUS_DP = 4
        private const val DEFAULT_SELECTED_DOT_RADIUS_DP = 5.5f
        private const val DEFAULT_DOT_SEPARATION_DISTANCE_DP = 10
    }

    private var recyclerView: RecyclerView? = null
    private var viewPager: ViewPager? = null
    private var viewPager2: ViewPager2? = null
    private var internalRecyclerScrollListener: InternalRecyclerScrollListener? = null
    private var internalPageChangeCallback: InternalPageChangeCallback? = null
    private val interpolator = DecelerateInterpolator()

    private var dotCount = DEFAULT_DOT_COUNT
    private var fadingDotCount = DEFAULT_FADING_DOT_COUNT
    private var selectedDotRadiusPx = dpToPx(dp = DEFAULT_SELECTED_DOT_RADIUS_DP)
    private var dotRadiusPx = dpToPx(dp = DEFAULT_DOT_RADIUS_DP.toFloat())
    private var dotSeparationDistancePx = dpToPx(dp = DEFAULT_DOT_SEPARATION_DISTANCE_DP.toFloat())
    private var verticalSupport = false

    @ColorInt
    private var dotColor: Int = ContextCompat.getColor(context, R.color.bandyer_colorOnSurface)

    @ColorInt
    private var selectedDotColor: Int = ContextCompat.getColor(context, R.color.bandyer_colorPrimary)

    private var selectedDotPaint: Paint
    private var dotPaint: Paint

    /**
     * The current pager position. Used to draw the selected dot if different size/color.
     */
    private var selectedItemPosition: Int = 0

    /**
     * A temporary value used to reflect changes/transition from one selected item to the next.
     */
    private var intermediateSelectedItemPosition: Int = 0

    /**
     * The scroll percentage of the viewpager or recyclerview.
     * Used for moving the dots/scaling the fading dots.
     */
    private var offsetPercent: Float = 0f

    init {
        dotCount = context.getPagerIndicatorIntAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorDotCount)
        fadingDotCount = context.getPagerIndicatorIntAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorFadingDotCount)
        dotRadiusPx = context.getPagerIndicatorDimensionPixelSizeAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorDotRadius)
        selectedDotRadiusPx = context.getPagerIndicatorDimensionPixelSizeAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorSelectedDotRadius)
        dotColor = context.getPagerIndicatorColorAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorDotColor)
        selectedDotColor = context.getPagerIndicatorColorAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorSelectedDotColor)
        dotSeparationDistancePx = context.getPagerIndicatorDimensionPixelSizeAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorDotSeparation)
        verticalSupport = context.getPagerIndicatorBooleanAttribute(R.styleable.BandyerSDKDesign_PagerIndicator_bandyer_pagerIndicatorVerticalSupport)

        selectedDotPaint = getDefaultPaintConfig(defaultColor = selectedDotColor)
        dotPaint = getDefaultPaintConfig(defaultColor = dotColor)
    }

    /**
     * On draw
     *
     * @param canvas
     * @suppress
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        (0 until getItemCount())
                .map { position -> getDotCoordinate(position = position) }
                .forEach { coordinate ->
                    val (xPosition: Float, yPosition: Float) = getXYPositionsByCoordinate(coordinate = coordinate)
                    canvas.drawCircle(
                            xPosition,
                            yPosition,
                            getRadius(coordinate = coordinate),
                            getPaint(coordinate = coordinate))
                }
    }

    /**
     * On measure
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     * @suppress
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minimumViewSize = 2 * selectedDotRadiusPx
        if (verticalSupport) setMeasuredDimension(minimumViewSize, getCalculatedWidth())
        else setMeasuredDimension(getCalculatedWidth(), minimumViewSize)
    }

    /**
     * Attach the pager indicator to a recyclerview
     * @param recyclerView RecyclerView?
     */
    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        removeAllSources()

        this.recyclerView = recyclerView

        InternalRecyclerScrollListener().let { newScrollListener ->
            internalRecyclerScrollListener = newScrollListener
            this.recyclerView?.addOnScrollListener(newScrollListener)
        }
    }

    /**
     * Attach the pager indicator to a view pager
     * @param viewPager ViewPager?
     */
    fun attachToViewPager(viewPager: ViewPager?) {
        removeAllSources()

        this.viewPager = viewPager
        this.viewPager?.addOnPageChangeListener(this)

        selectedItemPosition = viewPager?.currentItem ?: 0
    }

    /**
     * Attach the pager indicator to a view pager 2
     * @param viewPager2 ViewPager2
     */
    fun attachToViewPager2(viewPager2: ViewPager2) {
        removeAllSources()

        this.viewPager2 = viewPager2

        InternalPageChangeCallback().let {
            internalPageChangeCallback = it
            this.viewPager2?.registerOnPageChangeCallback(it)
        }

        selectedItemPosition = this.viewPager2?.currentItem ?: 0
    }

    private fun getDefaultPaintConfig(defaultStyle: Paint.Style = Paint.Style.FILL, isAntiAliasDefault: Boolean = true, @ColorInt defaultColor: Int): Paint = Paint().apply {
        style = defaultStyle
        isAntiAlias = isAntiAliasDefault
        color = defaultColor
    }

    private fun getXYPositionsByCoordinate(coordinate: Float): Pair<Float, Float> {
        val xPosition: Float
        val yPosition: Float
        if (verticalSupport) {
            xPosition = getDotYCoordinate().toFloat()
            yPosition = height / 2 + coordinate
        } else {
            xPosition = width / 2 + coordinate
            yPosition = getDotYCoordinate().toFloat()
        }
        return Pair(xPosition, yPosition)
    }

    private fun getDotCoordinate(position: Int): Float =
            (position - intermediateSelectedItemPosition) * getDistanceBetweenTheCenterOfTwoDots() + (getDistanceBetweenTheCenterOfTwoDots() * offsetPercent)

    /**
     * Get the y coordinate for a dot.
     *
     * The bottom of the view is y = 0 and a dot is drawn from the center, so therefore
     * the y coordinate is simply the radius.
     */
    private fun getDotYCoordinate(): Int = selectedDotRadiusPx

    /**
     * Calculates the distance between 2 dots center.
     */
    private fun getDistanceBetweenTheCenterOfTwoDots() = 2 * dotRadiusPx + dotSeparationDistancePx

    /**
     * Calculates a dot radius based on its position.
     *
     * If the position is within 1 dot length, it's the currently selected dot.
     *
     * If the position is within a threshold (half the width of the number of non fading dots),
     * it is a normal sized dot.
     *
     * If the position is outside of the above threshold, it is a fading dot or not visible. The
     * radius is calculated based on a interpolator percentage of how far the
     * viewpager/recyclerview has scrolled.
     */
    private fun getRadius(coordinate: Float): Float {
        val coordinateAbs = abs(coordinate)
        // Get the coordinate where dots begin showing as fading dots (x coordinates > half of width of all large dots)
        val largeDotThreshold = dotCount.toFloat() / 2 * getDistanceBetweenTheCenterOfTwoDots()
        return when {
            coordinateAbs < getDistanceBetweenTheCenterOfTwoDots() / 2 -> selectedDotRadiusPx.toFloat()
            coordinateAbs <= largeDotThreshold -> dotRadiusPx.toFloat()
            else -> {
                // Determine how close the dot is to the edge of the view for scaling the size of the dot
                val percentTowardsEdge = (coordinateAbs - largeDotThreshold) / (getCalculatedWidth() / 2.01f - largeDotThreshold)
                interpolator.getInterpolation(1 - percentTowardsEdge) * dotRadiusPx
            }
        }
    }

    /**
     * Returns the dot's color based on coordinate, similar to {@link #getRadius(Float)}.
     *
     * If the position is within 1 dot's length of x or y = 0, it is the currently selected dot.
     *
     * All other dots will be the normal specified dot color.
     */
    private fun getPaint(coordinate: Float): Paint = when {
        abs(coordinate) < getDistanceBetweenTheCenterOfTwoDots() / 2 -> selectedDotPaint
        else -> dotPaint
    }

    /**
     * Get the calculated width of the view.
     *
     * Calculated by the total number of visible dots (normal & fading).
     *
     */
    private fun getCalculatedWidth(): Int {
        val maxNumVisibleDots = dotCount + 2 * fadingDotCount
        return (maxNumVisibleDots - 1) * getDistanceBetweenTheCenterOfTwoDots() + 2 * dotRadiusPx
    }

    private fun dpToPx(dp: Float): Int = (dp * ((resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT).toFloat())).toInt()

    private fun removeAllSources() {
        internalRecyclerScrollListener?.let {
            recyclerView?.removeOnScrollListener(it)
        }

        this.viewPager?.removeOnPageChangeListener(this)

        internalPageChangeCallback?.let {
            viewPager2?.unregisterOnPageChangeCallback(it)
        }

        recyclerView = null
        viewPager = null
        viewPager2 = null
    }

    private fun getItemCount(): Int = when {
        recyclerView != null -> recyclerView?.adapter?.itemCount ?: 0
        viewPager != null -> viewPager?.adapter?.count ?: 0
        viewPager2 != null -> viewPager2?.adapter?.itemCount ?: 0
        else -> 0
    }

    private fun getRTLPosition(position: Int) = getItemCount() - position - 1

    /**
     * On page scrolled
     *
     * @param position
     * @param positionOffset
     * @param positionOffsetPixels
     * @suppress
     */
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (isRtl()) {
            val currentPosition = getRTLPosition(position = position)
            selectedItemPosition = currentPosition
            intermediateSelectedItemPosition = currentPosition
            offsetPercent = positionOffset * 1
        } else {
            selectedItemPosition = position
            intermediateSelectedItemPosition = position
            offsetPercent = positionOffset * -1
        }
        invalidate()
    }

    /**
     * On page selected
     *
     * @param position
     * @suppress
     */
    override fun onPageSelected(position: Int) {
        intermediateSelectedItemPosition = selectedItemPosition
        selectedItemPosition = if (isRtl()) getRTLPosition(position = position) else position
        invalidate()
    }

    /**
     * On page scroll state changed
     *
     * @param state
     * @suppress
     */
    override fun onPageScrollStateChanged(state: Int) = Unit

    internal inner class InternalRecyclerScrollListener : RecyclerView.OnScrollListener() {

        /**
         * The previous most visible child page in the RecyclerView.
         *
         * Used to differentiate between the current most visible child page to correctly determine
         * the currently selected item and percentage scrolled.
         */
        private var previousMostVisibleChild: View? = null

        /**
         * Determine based on the percentage a child viewholder's view is visible what position
         * is the currently selected.
         *
         * Use this percentage to also calculate the offsetPercentage
         * used to scale dots.
         */
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            val view = getMostVisibleChild()
            if (view != null) {
                setIntermediateSelectedItemPosition(mostVisibleChild = view)
                offsetPercent = view.left.toFloat() / view.measuredWidth
            }

            with(recyclerView.layoutManager as LinearLayoutManager) {
                val visibleItemPosition = if (dx >= 0) findLastVisibleItemPosition() else findFirstVisibleItemPosition()

                if (previousMostVisibleChild !== findViewByPosition(visibleItemPosition)) selectedItemPosition = intermediateSelectedItemPosition
            }

            previousMostVisibleChild = view
            invalidate()
        }

        /**
         * Returns the currently most visible viewholder view in the Recyclerview.
         *
         * The most visible view is determined based on percentage of the view visible. This is
         * calculated below in calculatePercentVisible().
         */
        private fun getMostVisibleChild(): View? {
            var mostVisibleChild: View? = null
            var mostVisibleChildPercent = 0f
            for (i in recyclerView?.layoutManager?.childCount!! - 1 downTo 0) {
                val child = recyclerView?.layoutManager?.getChildAt(i)
                if (child != null) {
                    val percentVisible = calculatePercentVisible(child = child)
                    if (percentVisible >= mostVisibleChildPercent) {
                        mostVisibleChildPercent = percentVisible
                        mostVisibleChild = child
                    }
                }
            }
            return mostVisibleChild
        }

        private fun calculatePercentVisible(child: View): Float {
            val left = child.left
            val right = child.right
            val width = child.width

            return when {
                left < 0 -> right / width.toFloat()
                right > getWidth() -> (getWidth() - left) / width.toFloat()
                else -> 1f
            }
        }

        private fun setIntermediateSelectedItemPosition(mostVisibleChild: View) {
            recyclerView?.findContainingViewHolder(mostVisibleChild)?.adapterPosition?.let { position ->
                intermediateSelectedItemPosition = if (isRtl() && !verticalSupport && (recyclerView?.layoutManager as? LinearLayoutManager)?.stackFromEnd == false) {
                    getRTLPosition(position = position)
                } else position
            }
        }
    }

    internal inner class InternalPageChangeCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            this.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            this.onPageSelected(position)
        }
    }
}