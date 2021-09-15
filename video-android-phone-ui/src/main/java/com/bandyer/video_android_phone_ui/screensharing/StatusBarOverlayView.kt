/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.video_android_phone_ui.screensharing

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.dp2px
import com.bandyer.video_android_phone_ui.R
import com.bandyer.video_android_phone_ui.extensions.getScreenSize
import com.bandyer.video_android_phone_ui.extensions.scanForFragmentActivity
import com.bandyer.video_android_phone_ui.utils.systemviews.SystemViewLayoutObserver
import com.bandyer.video_android_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener

/**
 * Status bar overlay view(a pulsing red alert to be placed under the status bar)
 *
 * @constructor
 *
 * @param context The Context the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 *        reference to a style resource that supplies default values for
 *        the view. Can be 0 to not look for defaults.
 */
class StatusBarOverlayView @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr), SystemViewLayoutObserver, View.OnLayoutChangeListener {


    private val STATUS_BAR_HEIGHT_LIMIT = context.dp2px(25f)


    private var statusBarHeight = STATUS_BAR_HEIGHT_LIMIT

    private var alphaAnimation: ValueAnimator? = null

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.addObserver(it, this)
        }
        alphaAnimation?.start()
        updateHeight()
        addOnLayoutChangeListener(this)
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.removeObserver(it as AppCompatActivity, this)
        }
        alphaAnimation?.cancel()
        removeOnLayoutChangeListener(this)
    }

    override fun onTopInsetChanged(pixels: Int) {
        if (pixels >= STATUS_BAR_HEIGHT_LIMIT) return
        statusBarHeight = pixels.takeIf { it > 0 } ?: statusBarHeight
        updateHeight()
    }

    private fun updateHeight(height: Int = statusBarHeight) {
        if (height > STATUS_BAR_HEIGHT_LIMIT || height == 0 || layoutParams?.height == height) return
        post {
            layoutParams ?: return@post
            if (layoutParams.height == height) return@post
            layoutParams.height = height
            requestLayout()
            bringToFront()
        }
    }

    override fun onBottomInsetChanged(pixels: Int) = Unit
    override fun onRightInsetChanged(pixels: Int) = Unit
    override fun onLeftInsetChanged(pixels: Int) = Unit

    private fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio

        val a = Color.alpha(to) * ratio + Color.alpha(from) * inverseRatio
        val r = Color.red(to) * ratio + Color.red(from) * inverseRatio
        val g = Color.green(to) * ratio + Color.green(from) * inverseRatio
        val b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio

        return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    }

    /**
     * @suppress
     */
    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (statusBarHeight == 0 || v == null || oldRight == right) return
        val multiplier = (right - left).toFloat() / v.context.getScreenSize().x
        if (multiplier > 1) return
        updateHeight((statusBarHeight * multiplier).toInt())
    }


    init {
        id = R.id.bandyer_id_screen_share_overlay

        alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
            this.repeatMode = ValueAnimator.REVERSE
            this.repeatCount = ValueAnimator.INFINITE
            this.duration = 2000L
            this.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) = bringToFront()
                override fun onAnimationStart(animation: Animator?) = bringToFront()
                override fun onAnimationEnd(animation: Animator?) = Unit
                override fun onAnimationCancel(animation: Animator?) = Unit
            })

            this.addUpdateListener { animation ->
                // Use animation position to blend colors.
                val position = animation.animatedFraction
                val color = ContextCompat.getColor(getContext(), R.color.bandyer_screen_share_color)
                val blended = blendColors(Color.TRANSPARENT, color, position)
                // Apply blended color to the view.
                setBackgroundColor(blended)
            }
        }
    }

}


