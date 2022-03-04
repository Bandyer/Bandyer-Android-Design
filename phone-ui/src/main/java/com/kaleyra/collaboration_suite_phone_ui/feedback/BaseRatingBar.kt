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

package com.kaleyra.collaboration_suite_phone_ui.feedback

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.dp2px
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.extensions.FloatExtensions.floor
import com.kaleyra.collaboration_suite_phone_ui.extensions.FloatExtensions.round
import com.kaleyra.collaboration_suite_phone_ui.extensions.MotionEventExtensions.isClickEvent
import com.kaleyra.collaboration_suite_phone_ui.extensions.isRtl
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A BaseRatingBar. By default it has 5 levels, stars as icons and a stepSize of 1.
 *
 * @constructor
 */
internal open class BaseRatingBar @JvmOverloads constructor(
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            saveAttributeDataForStyleable(context, R.styleable.BaseRatingBar, attrs, a, defStyleAttr, defStyleRes)

        numLevels = max(a.getInt(R.styleable.BaseRatingBar_kaleyra_numLevels, numLevels), 0)
        minRating = a.getFloat(R.styleable.BaseRatingBar_kaleyra_minRating, minRating).coerceIn(0f, numLevels.toFloat())
        rating = closestValueToStepSize(a.getFloat(R.styleable.BaseRatingBar_android_rating, minRating).coerceIn(minRating, numLevels.toFloat()).round(2))
        stepSize = a.getFloat(R.styleable.BaseRatingBar_android_stepSize, stepSize).coerceIn(0.1f, 1f)
        drawableSize = max(a.getDimension(R.styleable.BaseRatingBar_drawableSize, drawableSize), 0f)
        drawablePadding = max(a.getDimension(R.styleable.BaseRatingBar_android_drawablePadding, drawablePadding), 0f)
        drawableBackground = if (a.hasValue(R.styleable.BaseRatingBar_android_drawable)) ContextCompat.getDrawable(context, a.getResourceId(R.styleable.BaseRatingBar_android_drawable, NO_ID)) else ContextCompat.getDrawable(context, R.drawable.ic_kaleyra_empty_star)
        drawableProgress = if (a.hasValue(R.styleable.BaseRatingBar_android_progressDrawable)) ContextCompat.getDrawable(context, a.getResourceId(R.styleable.BaseRatingBar_android_progressDrawable, NO_ID)) else ContextCompat.getDrawable(context, R.drawable.ic_kaleyra_full_star)
        if (a.hasValue(R.styleable.BaseRatingBar_drawableTint)) a.getColor(R.styleable.BaseRatingBar_drawableTint, NO_ID).apply {
            drawableBackground = drawableBackground?.applyTint(this)
            drawableProgress = drawableProgress?.applyTint(this)
        }

        a.recycle()
        updateChildrenInternal(this.numLevels)
        setProgressInternal(this.rating)

        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO)
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!isEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                actionDownX = event.x
                actionDownY = event.y
            }
            MotionEvent.ACTION_MOVE -> handleTouchEvent(event.x, false)
            MotionEvent.ACTION_UP -> {
                if(!isClickable && event.isClickEvent(actionDownX, actionDownY)) return false
                handleTouchEvent(event.x, true)
            }
        }

        return true
    }

    protected open fun updateChildren(numLevels: Int) {
        updateChildrenInternal(numLevels)
    }

    private fun updateChildrenInternal(numLevels: Int) {
        val diff = numLevels - childCount
        if(diff > 0)
            for(i in 0 until diff) addView(BaseRatingBarElement(context, drawableProgress!!, drawableBackground!!, drawableSize.toInt(), drawablePadding.toInt()))
        else
            for(i in 0 until -diff) removeViewAt(childCount - 1)
    }

    protected open fun setProgress(rating: Float) {
        setProgressInternal(rating)
    }

    private fun setProgressInternal(rating: Float) {
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
        sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
    }

    private fun closestValueToStepSize(value: Float) = value - (value % stepSize).floor(1)

    private fun handleTouchEvent(eventX: Float, isActionUp: Boolean) =
        children.forEachIndexed { index, child ->
            if (!isTouchEventInChild(eventX, child)) return@forEachIndexed

            val rating =
                if (stepSize == 1f) index + 1f
                else computeChildProgress(index, child, stepSize, eventX)

            if(isActionUp) onRatingChangeListener?.onRatingConfirmed(rating)
            setRating(rating)
        }

    private fun isTouchEventInChild(eventX: Float, child: View): Boolean =
        eventX > child.left && eventX < child.right

    private fun computeChildProgress(childIndex: Int, child: View, stepSize: Float, eventX: Float): Float {
        val diffX = if (isRtl()) child.right - eventX else eventX - child.left
        val ratio = (diffX / child.width).round(2)
        val steps = (ratio / stepSize).roundToInt() * stepSize
        return (childIndex + 1 - (1 - steps)).round(2)
    }

    private fun Drawable.applyTint(drawableTint: Int): Drawable =
        DrawableCompat.wrap(this).apply {
            setTintMode(PorterDuff.Mode.SRC_IN)
            setTint(drawableTint)
        }

    override fun getAccessibilityClassName(): CharSequence =
        BaseRatingBar::class.java.name

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.itemCount = numLevels
        event.currentItemIndex = rating.roundToInt()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.rangeInfo = AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT, 0f, numLevels.toFloat(), rating)

        if (!isEnabled) return

        if (rating > minRating)
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)
        if (rating < numLevels)
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS)
    }

    override fun performAccessibilityAction(action: Int, args: Bundle?): Boolean {
        super.performAccessibilityAction(action, args)

        if (!isEnabled) return false

        when (action) {
            android.R.id.accessibilityActionSetProgress -> {
                if (args == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !args.containsKey(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE))
                    return false

                val value = args.getFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE)
                setRating(value)
                return true
            }
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> {
                return if(rating < numLevels) {
                    setRating(rating + stepSize)
                    true
                } else false
            }
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> {
                return if(rating > minRating) {
                    setRating(rating - stepSize)
                    true
                } else false
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isEnabled) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MINUS -> {
                    val stepSize = if (isRtl()) stepSize else -stepSize
                    setRating(rating + stepSize)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_EQUALS -> {
                    val stepSize = if (isRtl()) -stepSize else stepSize
                    setRating(rating + stepSize)
                    return true
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    private companion object {
        const val LEVELS = 5
        const val STEP_SIZE = 1f
        const val ICON_SIZE = 36f
    }
}