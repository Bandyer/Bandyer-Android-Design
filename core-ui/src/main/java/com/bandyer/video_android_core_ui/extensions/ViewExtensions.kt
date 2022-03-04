/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bandyer.video_android_core_ui.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Build
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd

/**
 * View extensions
 */
object ViewExtensions {


    /**
     * Set the bottom padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    fun View.setPaddingBottom(@Px px: Int) {
        this.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, px)
    }

    /**
     * Set the end padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun View.setPaddingEnd(@Px px: Int) {
        this.setPaddingRelative(this.paddingStart, this.paddingTop, px, this.paddingBottom)
    }

    /**
     * Set the horizontal padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    fun View.setPaddingHorizontal(@Px px: Int) {
        this.setPadding(px, this.paddingTop, px, this.paddingBottom)
    }

    /**
     * Set the left padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    fun View.setPaddingLeft(@Px px: Int) {
        this.setPadding(px, this.paddingTop, this.paddingRight, this.paddingBottom)
    }

    /**
     * Set the right padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    fun View.setPaddingRight(@Px px: Int) {
        this.setPadding(this.paddingLeft, this.paddingTop, px, this.paddingBottom)
    }

    /**
     * Set the start padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun View.setPaddingStart(@Px px: Int) {
        this.setPaddingRelative(px, this.paddingTop, this.paddingEnd, this.paddingBottom)
    }

    /**
     * Set the top padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    fun View.setPaddingTop(@Px px: Int) {
        this.setPadding(this.paddingLeft, px, this.paddingRight, this.paddingBottom)
    }

    /**
     * Set the vertical padding for a view
     * @receiver View
     * @param px The padding to be set expressed in pixel
     */
    fun View.setPaddingVertical(@Px px: Int) {
        this.setPadding(this.paddingLeft, px, this.paddingRight, px)
    }

    /**
     * Smoothly animates view's height from size to desired size
     * @receiver View
     * @param from starting size
     * @param to desired final size
     * @param interpolator animator interpolator
     */
    fun View.animateViewHeight(
        from: Int,
        to: Int,
        duration: Long,
        interpolator: Interpolator = LinearInterpolator(),
        doOnEnd: ((Animator) -> Unit)? = null
    ) {
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

    /**
     * Set the alpha with a fade animation
     *
     * @receiver View to which apply the alpha
     * @param targetAlpha Float
     * @param duration The duration in millis
     */
    fun View.setAlphaWithAnimation(targetAlpha: Float, duration: Long) =
        this.animate()
            .alpha(targetAlpha)
            .also {
                it.duration = duration
            }.start()
}