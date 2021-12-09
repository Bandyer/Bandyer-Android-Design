package com.bandyer.sdk_design.feedback

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.FloatExtensions.floor
import com.bandyer.sdk_design.extensions.FloatExtensions.round
import com.bandyer.sdk_design.extensions.MotionEventExtensions.isClickEvent
import com.bandyer.sdk_design.extensions.dp2px
import com.bandyer.sdk_design.extensions.isRtl
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * A BaseRatingBar. By default it has 5 levels, stars as icons and a stepSize of 1.
 *
 * @constructor
 */
internal abstract class BaseRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), RatingBar {

    var onRatingChangeListener: RatingBar.OnRatingChangeListener? = null

    private var numLevels: Int = LEVELS
    private var stepSize: Float = STEP_SIZE
    private var rating: Float = 0f
    private var minRating: Float = 0f

    private var drawableSize: Float = context.dp2px(ICON_SIZE).toFloat()
    private var drawablePadding: Float = 0f
    private var drawableProgress: Drawable? = null
    private var drawableBackground: Drawable? = null

    private var actionDownX: Float = 0f
    private var actionDownY: Float = 0f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BaseRatingBar, defStyleAttr, defStyleRes)
        ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.BaseRatingBar, attrs, a, defStyleAttr, defStyleRes)

        val numLevels = a.getInt(R.styleable.BaseRatingBar_bandyer_numLevels, numLevels)
        val minRating = a.getFloat(R.styleable.BaseRatingBar_bandyer_minRating, minRating)
        val rating = a.getFloat(R.styleable.BaseRatingBar_android_rating, minRating)
        val stepSize = a.getFloat(R.styleable.BaseRatingBar_android_stepSize, stepSize)
        val drawablePadding = a.getDimension(R.styleable.BaseRatingBar_android_drawablePadding, drawablePadding)
        val drawableTint = if (a.hasValue(R.styleable.BaseRatingBar_drawableTint)) a.getColor(R.styleable.BaseRatingBar_drawableTint, NO_ID) else null
        val drawableBackground = if (a.hasValue(R.styleable.BaseRatingBar_android_drawable)) ContextCompat.getDrawable(context, a.getResourceId(R.styleable.BaseRatingBar_android_drawable, NO_ID)) else null
        val drawableProgress = if (a.hasValue(R.styleable.BaseRatingBar_android_progressDrawable)) ContextCompat.getDrawable(context, a.getResourceId(R.styleable.BaseRatingBar_android_progressDrawable, NO_ID)) else null
        val drawableSize = a.getDimension(R.styleable.BaseRatingBar_drawableSize, drawableSize)

        a.recycle()
        verifyParams(numLevels, minRating, rating, stepSize, drawablePadding, drawableTint, drawableBackground, drawableProgress, drawableSize)
    }

    private fun verifyParams(numLevels: Int, minRating: Float, rating: Float, stepSize: Float, drawablePadding: Float, drawableTint: Int?, drawableBackground: Drawable?, drawableProgress: Drawable?, drawableSize: Float) {
        if (numLevels > 0)
            this.numLevels = numLevels

        if (stepSize in 0.1f..1f)
            this.stepSize = stepSize.round(2)

        if (minRating in 0f..this.numLevels.toFloat())
            this.minRating = minRating

        this.rating =
            if (rating in this.minRating..this.numLevels.toFloat()) closestValueToStepSize(rating.round(2))
            else this.minRating

        if (drawableSize > 0)
            this.drawableSize = drawableSize

        if (drawablePadding > 0)
            this.drawablePadding = drawablePadding

        this.drawableBackground = drawableBackground ?: ContextCompat.getDrawable(context, R.drawable.ic_bandyer_empty_star)

        this.drawableProgress = drawableProgress ?: ContextCompat.getDrawable(context, R.drawable.ic_bandyer_full_star)

        if(drawableTint != null) {
            this.drawableBackground = this.drawableBackground?.applyTint(drawableTint)
            this.drawableProgress = this.drawableProgress?.applyTint(drawableTint)
        }
    }

    override fun setNumLevels(@IntRange(from = 0) numLevels: Int) {
        this.numLevels = numLevels
        updateChildren(numLevels)
    }

    override fun getNumLevels(): Int = this.numLevels

    override fun setRating(value: Float) {
        if (value == rating || value !in minRating..numLevels.toFloat()) return

        rating = closestValueToStepSize(value.round(2))
        setProgress(rating)
        onRatingChangeListener?.onRatingChange(rating)
    }

    override fun getRating(): Float = rating

    override fun setStepSize(@FloatRange(from = 0.1, to = 1.0) stepSize: Float) {
        this.stepSize = stepSize.round(2)
        setRating(closestValueToStepSize(rating))
    }

    override fun getStepSize(): Float = stepSize

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

    protected open fun updateChildren(numLevels: Int) {
        val diff = numLevels - childCount
        if(diff > 0)
            for(i in 0 until diff) addView(BaseRatingBarElement(context, drawableProgress!!, drawableBackground!!, drawableSize.toInt(), drawablePadding.toInt()))
        else
            for(i in 0 until -diff) removeViewAt(childCount - 1)
    }

    protected open fun setProgress(rating: Float) {
        children.forEachIndexed { index, child ->
            val intFloor = floor(rating.toDouble()).toInt()
            (child as BaseRatingBarElement).setProgress(
                when {
                    index > intFloor -> 0f
                    index == intFloor -> rating - intFloor
                    else -> 1f
                }
            )
        }
    }

    private fun closestValueToStepSize(value: Float) = value - (value % stepSize).floor(1)

    private fun handleTouchEvent(eventX: Float, isClickEvent: Boolean) =
        children.forEachIndexed { index, child ->
            if (!isTouchEventInChild(eventX, child)) return@forEachIndexed

            val rating =
                if (isClickEvent && stepSize == 1f) index + 1f
                else computeChildProgress(index, child, stepSize, eventX)

            setRating(rating)
        }

    private fun isTouchEventInChild(eventX: Float, child: View): Boolean =
        eventX > child.left && eventX < child.right

    private fun computeChildProgress(childIndex: Int, child: View, stepSize: Float, eventX: Float): Float {
        val diffX = if (context.isRtl()) child.right - eventX else eventX - child.left
        val ratio = (diffX / child.width).round(2)
        val steps = (ratio / stepSize).roundToInt() * stepSize
        return (childIndex + 1 - (1 - steps)).round(2)
    }

    private fun Drawable.applyTint(drawableTint: Int): Drawable =
        DrawableCompat.wrap(this).apply {
            setTintMode(PorterDuff.Mode.SRC_IN)
            setTint(drawableTint)
        }

    private companion object {
        const val LEVELS = 5
        const val STEP_SIZE = 1f
        const val ICON_SIZE = 36f
    }
}