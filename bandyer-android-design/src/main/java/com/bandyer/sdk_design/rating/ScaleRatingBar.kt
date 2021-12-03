package com.bandyer.sdk_design.rating

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.children
import androidx.core.view.doOnLayout
import kotlin.math.floor

internal class ScaleRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseRatingBar(context, attrs, defStyleAttr) {

    init {
//       initItems()
    }

    private val itemsTargetScale: MutableMap<View, Float> = mutableMapOf()

    override fun setProgress(rating: Float) {
        super.setProgress(rating)

        post {
            children.forEachIndexed { index, item ->
            val intFloor = floor(rating.toDouble()).toInt()
            when {
                index > intFloor - 1 -> {
                    if(itemsTargetScale[item] == 0.75f) return@forEachIndexed
                    itemsTargetScale[item] = 0.75f
                    item.scale(SCALE_DOWN_VALUE, ANIMATION_DURATION, DecelerateInterpolator())
                }
                index == intFloor - 1 -> {
                    if(itemsTargetScale[item] == 1f) return@forEachIndexed
                    itemsTargetScale[item] = 1f
                    item.scale(1f, ANIMATION_DURATION, DecelerateInterpolator())
                }
                else -> {
                    if(itemsTargetScale[item] == 1f) return@forEachIndexed
                    itemsTargetScale[item] = 1f
                    item.scale(1f, ANIMATION_DURATION, DecelerateInterpolator())
                }
            }
        }
        }
    }

    private fun initItems() =
        doOnLayout {
            children.forEach {
                it.scaleX = SCALE_DOWN_VALUE
                it.scaleY = SCALE_DOWN_VALUE
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