package com.bandyer.sdk_design.rating

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.doOnLayout
import kotlin.math.floor

internal class ScaleRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseRatingBar(context, attrs, defStyleAttr) {

    init {
       initItems()
    }

    override fun setProgress(rating: Float) {
        super.setProgress(rating)

        items.forEachIndexed { index, item ->
            val intFloor = floor(rating.toDouble()).toInt()
            when {
                index > intFloor - 1 -> item.scale(SCALE_DOWN_VALUE, ANIMATION_DURATION, DecelerateInterpolator())
                index == intFloor - 1 -> item.scale(1f, ANIMATION_DURATION, DecelerateInterpolator())
                else -> item.scale(1f, ANIMATION_DURATION, DecelerateInterpolator())
            }
        }
    }

    private fun initItems() =
        doOnLayout {
            items.forEach { it.scale(SCALE_DOWN_VALUE, 0, DecelerateInterpolator()) }
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