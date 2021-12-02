package com.bandyer.sdk_design.rating

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.FloatExtensions.round
import com.bandyer.sdk_design.extensions.MotionEventExtensions.isClickEvent
import com.bandyer.sdk_design.extensions.dp2px
import java.util.ArrayList
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * A BaseRatingBar. By default it has 5 levels, stars as icons and a stepSize of 1.
 *
 * @constructor
 */
internal open class BaseRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), RatingBar {

    // nStar, star width, star height, star padding, emptyDrawable, filledDrawable, stepSize

    val onRatingChangeListener: RatingBar.OnRatingChangeListener? = null

    private var rating: Float = 0f

    protected var items: ArrayList<BaseRatingBarItem> = arrayListOf()

    private var nLevels = DEFAULT_N_LEVELS
    private var stepSize = DEFAULT_STEP_SIZE

    private var itemIconSize = context.dp2px(48f)
    private var itemPadding = context.dp2px(8f)
    private var itemProgressDrawable: Drawable? = null
    private var itemBackgroundDrawable: Drawable? = null

    private var actionDownX = 0f
    private var actionDownY = 0f
    private var previousRating = 0f

    init {
        // TODO get attributes from xml
        isClickable = true
        getParams()
        verifyParams()
        populateBar()
    }

    private fun getParams() {

    }

    private fun verifyParams() {
        if (nLevels <= 0) nLevels = DEFAULT_N_LEVELS

        if (itemPadding < 0) itemPadding = 0

        if (itemProgressDrawable == null)
            itemProgressDrawable = ContextCompat.getDrawable(context, R.drawable.ic_bandyer_full_star)

        if (itemBackgroundDrawable == null)
            itemBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.ic_bandyer_empty_star)

        stepSize = when {
            stepSize > 1.0f -> 1.0f
            stepSize < 0.1f -> 0.1f
            else -> stepSize
        }
    }

    private fun populateBar() {
        for (i in 0 until nLevels) {
            val itemView = BaseRatingBarItem(context, itemProgressDrawable!!, itemBackgroundDrawable!!, itemIconSize, itemPadding)
            addView(itemView)
            items.add(itemView)
        }
    }

    override fun setNumOfLevels(@IntRange(from = 0) nLevels: Int) {
        items.clear()
        removeAllViews()
        this.nLevels = nLevels
        populateBar()
    }

    override fun setRating(value: Float) {
        if (value == rating) return

        val roundedValue = value.round(2)
        rating = when {
            roundedValue > nLevels -> nLevels.toFloat()
            roundedValue < 0 -> 0f
            else -> roundedValue - roundedValue % stepSize
        }

        setProgress(rating)
        onRatingChangeListener?.onRatingChange(rating)
    }

    override fun getRating(): Float = rating

    override fun setStepSize(@FloatRange(from = 0.1, to = 1.0) stepSize: Float) { this.stepSize = stepSize }

    override fun getStepSize(): Float = stepSize

    protected open fun setProgress(rating: Float) =
        items.forEachIndexed { index, item ->
            val intFloor = floor(rating.toDouble()).toInt()
            item.setProgress(
                when {
                    index > intFloor -> 0f
                    index == intFloor -> rating - intFloor
                    else -> 1f
                }
            )
        }

    override fun onInterceptTouchEvent(ev: MotionEvent?) = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                actionDownX = event.x
                actionDownY = event.y
                previousRating = rating
            }
            MotionEvent.ACTION_MOVE -> handleMoveEvent(event.x)
            MotionEvent.ACTION_UP -> {
                if (!isClickable || !event.isClickEvent(actionDownX, actionDownY)) return false
                handleClickEvent(event.x)
            }
        }

        parent.requestDisallowInterceptTouchEvent(true)
        return true
    }

    private fun handleMoveEvent(eventX: Float) =
        items.forEachIndexed { index, item ->
            if (eventX < item.width / 10f) {
                setRating(0f)
                return
            }
            if (!isTouchEventInRatingItem(eventX, item)) return@forEachIndexed

            setRating(computeRating(index, item, stepSize, eventX))
        }

    private fun handleClickEvent(eventX: Float) =
        items.forEachIndexed { index, item ->
            if (!isTouchEventInRatingItem(eventX, item)) return@forEachIndexed

            setRating(
                if (stepSize == 1f) index + 1f
                else computeRating(index, item, stepSize, eventX)
            )
        }

    private fun isTouchEventInRatingItem(eventX: Float, ratingView: View): Boolean =
        eventX > ratingView.left && eventX < ratingView.right

    private fun computeRating(itemIndex: Int, item: BaseRatingBarItem, stepSize: Float, eventX: Float): Float {
        val ratio = ((eventX - item.left) / item.width).round(2)
        val steps = (ratio / stepSize).roundToInt() * stepSize
        return (itemIndex + 1 - (1 - steps)).round(2)
    }

    companion object {
        const val DEFAULT_N_LEVELS = 5
        const val DEFAULT_STEP_SIZE = 1f
    }
}