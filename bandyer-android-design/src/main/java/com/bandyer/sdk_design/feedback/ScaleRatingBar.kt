package com.bandyer.sdk_design.feedback

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.children
import kotlin.math.floor
import kotlin.math.roundToInt

internal class ScaleRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseRatingBar(context, attrs, defStyleAttr) {

    private val childrenScales: MutableMap<View, Float> = mutableMapOf()

    init {
        updateChildren(getNumLevels())
        setProgress(getRating())
    }

    override fun setProgress(rating: Float) {
        super.setProgress(rating)
        children.forEachIndexed { index, item ->
            val intFloor = floor(rating.toDouble()).toInt()
            when {
                index > intFloor - 1 -> {
                    if(childrenScales[item] == 0.75f) return@forEachIndexed
                    childrenScales[item] = 0.75f
                    item.scale(SCALE_DOWN_VALUE, ANIMATION_DURATION, DecelerateInterpolator())
                }
                index == intFloor - 1 -> {
                    if(childrenScales[item] == 1f) return@forEachIndexed
                    childrenScales[item] = 1f
                    item.scale(1f, ANIMATION_DURATION, DecelerateInterpolator())
                }
                else -> {
                    if(childrenScales[item] == 1f) return@forEachIndexed
                    childrenScales[item] = 1f
                    item.scale(1f, ANIMATION_DURATION, DecelerateInterpolator())
                }
            }
        }
    }

    override fun updateChildren(numLevels: Int) {
        super.updateChildren(numLevels)
        children.forEachIndexed { index, child ->
            if(index <= getRating().roundToInt()) return@forEachIndexed
            child.scaleX = SCALE_DOWN_VALUE
            child.scaleY = SCALE_DOWN_VALUE
        }
    }

    private fun View.scale(value: Float, duration: Long, interpolator: Interpolator) =
        animate()
            .scaleX(value)
            .scaleY(value)
            .setDuration(duration)
            .setInterpolator(interpolator)

    private companion object {
        const val ANIMATION_DURATION = 100L
        const val SCALE_DOWN_VALUE = .75f
    }
}