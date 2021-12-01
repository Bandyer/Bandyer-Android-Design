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
import com.bandyer.sdk_design.extensions.dp2px
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

internal open class RatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // nStar, star width, star height, star padding, emptyDrawable, filledDrawable, stepSize

    interface OnRatingChangeListener {
        fun onRatingChange(rating: Float)
    }

    val onRatingChangeListener: OnRatingChangeListener? = null

    private var rating: Float = 0f

    private var items: ArrayList<RatingBarItem> = arrayListOf()

    private var nStars = DEFAULT_N_STARS
    private var stepSize = DEFAULT_STEP_SIZE

    private var itemIconSize = context.dp2px(36f)
    private var itemPadding = 0
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
        if (nStars <= 0) nStars = DEFAULT_N_STARS

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
        for (i in 0 until nStars) {
            val itemView = RatingBarItem(context, itemProgressDrawable!!, itemBackgroundDrawable!!, itemIconSize, itemPadding)
            addView(itemView)
            items.add(itemView)
        }
    }

    fun setNumStars(@IntRange(from = 0) numStars: Int) {
        items.clear()
        removeAllViews()
        nStars = numStars
        populateBar()
    }


    fun setRating(value: Float) {
        if (value == rating) return

        rating = when {
            value > nStars -> nStars.toFloat()
            value < 0 -> 0f
            else -> floor(value / stepSize) * stepSize
        }

        setProgress(rating)
        onRatingChangeListener?.onRatingChange(rating)
    }

    fun getRating(): Float = rating

    fun setStepSize(@FloatRange(from = 0.1, to = 1.0) stepSize: Float) { this.stepSize = stepSize }

    fun getStepSize(): Float = stepSize

    private fun setProgress(rating: Float) {
        for (i in 0 until items.count()) {
            val maxIntOfRating = ceil(rating.toDouble()).toInt()
            items[i].setProgress(
                when {
                    i > maxIntOfRating -> 0f
                    i == maxIntOfRating -> rating
                    else -> 1f
                }
            )
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?) = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                actionDownX = eventX
                actionDownY = eventY
                previousRating = rating
            }
            MotionEvent.ACTION_MOVE -> {
                handleMoveEvent(eventX)
            }
            MotionEvent.ACTION_UP -> {
                if (!isClickable || !event.isClickEvent(actionDownX, actionDownY)) return false
                
                handleClickEvent(eventX)
            }
        }
        parent.requestDisallowInterceptTouchEvent(true)

        return true
    }

    private fun handleMoveEvent(eventX: Float) {
        for (i in 0 until items.count()) {
            val item = items[i]
            if (eventX < item.width / 10f) {
                setRating(0f)
                return
            }
            if (!isPositionInRatingView(eventX, item)) continue

            val rating = calculateRating(item, i, stepSize, eventX)

            setRating(rating)
        }
    }

    private fun handleClickEvent(eventX: Float) {
        for (i in 0 until items.count()) {
            val item = items[i]
            if (!isPositionInRatingView(eventX, item)) continue

            val rating = if (stepSize == 1f) i.toFloat() else calculateRating(item, i, stepSize, eventX)

            setRating(rating)
            break
        }
    }

    private fun isPositionInRatingView(eventX: Float, ratingView: View): Boolean =
        eventX > ratingView.left && eventX < ratingView.right

    private fun MotionEvent.isClickEvent(startX: Float, startY: Float): Boolean =
        if ((eventTime - downTime) > MAX_CLICK_DURATION) false
        else !(abs(startX - x) > MAX_CLICK_DISTANCE || abs(startY - y) > MAX_CLICK_DISTANCE)

    private fun calculateRating(item: RatingBarItem, position: Int, stepSize: Float, eventX: Float): Float {
        val decimalFormat: DecimalFormat = getDecimalFormat()
        val ratioOfView: Float = decimalFormat.format((eventX - item.left) / item.width).toFloat()
        val steps = (ratioOfView / stepSize).roundToInt() * stepSize
        return decimalFormat.format((position + 1 - (1 - steps)).toDouble()).toFloat()
    }

    private fun getDecimalFormat(): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.ENGLISH)
        symbols.decimalSeparator = '.'
        return DecimalFormat("#.##", symbols)
    }

    companion object {
        const val DEFAULT_N_STARS = 5
        const val DEFAULT_STEP_SIZE = 0.3f
        const val MAX_CLICK_DURATION = 200
        const val MAX_CLICK_DISTANCE = 5
    }
}