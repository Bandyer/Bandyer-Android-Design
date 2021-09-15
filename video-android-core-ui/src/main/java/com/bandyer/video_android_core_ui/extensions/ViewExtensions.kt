package com.bandyer.video_android_core_ui.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd

object ViewExtensions {

    /**
     * Smoothly animates view's height from size to desired size
     * @receiver View
     * @param from starting size
     * @param to desired final size
     * @param interpolator animator interpolator
     */
    fun View.animateViewHeight(from: Int, to: Int, duration: Long, interpolator: Interpolator = LinearInterpolator(), doOnEnd: ((Animator) -> Unit)? = null) {
        val valueAnimator = ValueAnimator.ofFloat(from.toFloat(), to.toFloat())
        valueAnimator.interpolator = interpolator
        valueAnimator.duration = duration
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            this.layoutParams.height = value.toInt()
            this.requestLayout()
        }
        doOnEnd?.also { valueAnimator.doOnEnd(it) }
        valueAnimator.start()
    }
}