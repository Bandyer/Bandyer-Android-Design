package com.bandyer.video_android_phone_ui.feedback

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
        updateChildrenScale()
    }

    override fun setProgress(rating: Float) {
        super.setProgress(rating)
        children.forEachIndexed { index, item ->
            val flooredRating = floor(rating.toDouble()).toInt()
            val targetScale = when {
                index > flooredRating - 1 -> SCALE_DOWN_VALUE
                else -> 1f
            }
            if(childrenScales[item] == targetScale) return@forEachIndexed
            childrenScales[item] = targetScale
            item.scale(targetScale, ANIMATION_DURATION, DecelerateInterpolator())
        }
    }

    override fun updateChildren(numLevels: Int) {
        super.updateChildren(numLevels)
        updateChildrenScale()
    }

    private fun updateChildrenScale() {
        children.forEachIndexed { index, child ->
            if(index < getRating().roundToInt()) return@forEachIndexed
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