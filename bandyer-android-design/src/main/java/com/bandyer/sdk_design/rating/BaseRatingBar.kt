package com.bandyer.sdk_design.rating

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.FloatExtensions.round
import com.bandyer.sdk_design.extensions.MotionEventExtensions.isClickEvent
import com.bandyer.sdk_design.extensions.dp2px
import com.bandyer.sdk_design.extensions.isRtl
import java.util.ArrayList
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * A BaseRatingBar. By default it has 5 levels, stars as icons and a stepSize of 0.5.
 *
 * @constructor
 */
internal open class BaseRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), RatingBar {

    val onRatingChangeListener: RatingBar.OnRatingChangeListener? = null

    protected var ratingBarElements: ArrayList<BaseRatingBarElement> = arrayListOf()

    private var numLevels: Int = 5
    private var nunMinLevels: Float = 2f
    private var stepSize: Float = 1f
    private var rating: Float = nunMinLevels

    private var iconSize: Float = context.dp2px(36f).toFloat()
    private var iconPadding: Float = 0f
    private var iconProgress: Drawable? = null
    private var iconBackground: Drawable? = null

    private var actionDownX: Float = 0f
    private var actionDownY: Float = 0f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BaseRatingBar, defStyleAttr, defStyleRes)
        ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.BaseRatingBar, attrs, a, defStyleAttr, defStyleRes)

        val numLevels = a.getInt(R.styleable.BaseRatingBar_bandyer_numLevels, numLevels)
        val nunMinLevels = a.getFloat(R.styleable.BaseRatingBar_bandyer_numMinLevels, nunMinLevels)
        val rating = a.getFloat(R.styleable.BaseRatingBar_android_rating, nunMinLevels)
        val stepSize = a.getFloat(R.styleable.BaseRatingBar_android_stepSize, stepSize)
        val iconPadding = a.getDimension(R.styleable.BaseRatingBar_bandyer_iconPadding, iconPadding)
        val iconBackground = if (a.hasValue(R.styleable.BaseRatingBar_bandyer_iconBackground)) ContextCompat.getDrawable(context, a.getResourceId(R.styleable.BaseRatingBar_bandyer_iconBackground, NO_ID)) else null
        val iconProgress = if (a.hasValue(R.styleable.BaseRatingBar_bandyer_iconProgress)) ContextCompat.getDrawable(context, a.getResourceId(R.styleable.BaseRatingBar_bandyer_iconProgress, NO_ID)) else null
        val iconSize = a.getDimension(R.styleable.BaseRatingBar_bandyer_iconSize, iconSize)

        a.recycle()

        verifyParams(numLevels, nunMinLevels, rating, stepSize, iconPadding, iconBackground, iconProgress, iconSize)
        populate()
    }

    private fun verifyParams(numLevels: Int, nunMinLevels: Float, rating: Float, stepSize: Float, iconPadding: Float, iconBackground: Drawable?, iconProgress: Drawable?, iconSize: Float) {
        if (numLevels > 0)
            this.numLevels = numLevels

        if (stepSize in 0.1f..1f)
            this.stepSize = stepSize.round(2)

        if (nunMinLevels in 0f..this.numLevels.toFloat())
            this.nunMinLevels = nunMinLevels

        if (rating in this.nunMinLevels..this.numLevels.toFloat())
            this.rating = closestValueToStepSize(rating.round(2))

        if (iconSize > 0)
            this.iconSize = iconSize

        if (iconPadding > 0)
            this.iconPadding = iconPadding

        this.iconProgress = iconProgress ?: ContextCompat.getDrawable(context, R.drawable.ic_bandyer_full_star)

        this.iconBackground = iconBackground ?: ContextCompat.getDrawable(context, R.drawable.ic_bandyer_empty_star)

//        val unwrappedDrawable: Drawable =
//            AppCompatResources.getDrawable(context, R.drawable.my_drawable)
//        val wrappedDrawable: Drawable = DrawableCompat.wrap(unwrappedDrawable)
//        DrawableCompat.setTint(wrappedDrawable, Color.RED)
//        DrawableCompat.setTint(
//            DrawableCompat.wrap(myImageView.getDrawable()),
//            ContextCompat.getColor(context, R.color.another_nice_color)
//        );
    }

    private fun populate() {
        removeAllViews()
        ratingBarElements.clear()

        for (index in 0 until numLevels) {
            val element = BaseRatingBarElement(
                context,
                iconProgress!!,
                iconBackground!!,
                iconSize.toInt(),
                iconPadding.toInt()
            )
            addView(element)
            ratingBarElements.add(element)
        }

        setProgress(rating)
    }

    override fun setNumLevels(@IntRange(from = 0) numLevels: Int) {
        this.numLevels = numLevels
        populate()
    }

    override fun setRating(value: Float) {
        if (value == rating || value !in nunMinLevels..numLevels.toFloat()) return

        rating = closestValueToStepSize(value.round(2))

        setProgress(rating)
        onRatingChangeListener?.onRatingChange(rating)
    }

    override fun getRating(): Float = rating

    override fun setStepSize(@FloatRange(from = 0.1, to = 1.0) stepSize: Float) {
        this.stepSize = stepSize.round(2)
    }

    override fun getStepSize(): Float = stepSize

    protected open fun setProgress(rating: Float) =
        ratingBarElements.forEachIndexed { index, element ->
            val intFloor = floor(rating.toDouble()).toInt()
            element.setProgress(
                when {
                    index > intFloor -> 0f
                    index == intFloor -> rating - intFloor
                    else -> 1f
                }
            )
        }

    private fun closestValueToStepSize(value: Float) = value - value % stepSize

    override fun onInterceptTouchEvent(ev: MotionEvent?) = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                actionDownX = event.x
                actionDownY = event.y
            }
            MotionEvent.ACTION_MOVE -> handleTouchEvent(event.x, false)
            MotionEvent.ACTION_UP -> {
                if (!isClickable || !event.isClickEvent(actionDownX, actionDownY)) return false
                handleTouchEvent(event.x, true)
            }
        }

        parent.requestDisallowInterceptTouchEvent(true)
        return true
    }

    private fun handleTouchEvent(eventX: Float, isClickEvent: Boolean) =
        ratingBarElements.forEachIndexed { index, element ->
            if (!isTouchEventInRatingElement(eventX, element)) return@forEachIndexed

            val rating =
                if (isClickEvent && stepSize == 1f) index + 1f
                else computeElementProgress(index, element, stepSize, eventX)

            setRating(rating)
        }

    private fun isTouchEventInRatingElement(eventX: Float, ratingView: View): Boolean =
        eventX > ratingView.left && eventX < ratingView.right

    private fun computeElementProgress(
        elementIndex: Int,
        element: BaseRatingBarElement,
        stepSize: Float,
        eventX: Float
    ): Float {
        val diffX = if (context.isRtl()) element.right - eventX else eventX - element.left
        val ratio = (diffX / element.width).round(2)
        val steps = (ratio / stepSize).roundToInt() * stepSize
        return (elementIndex + 1 - (1 - steps)).round(2)
    }
}