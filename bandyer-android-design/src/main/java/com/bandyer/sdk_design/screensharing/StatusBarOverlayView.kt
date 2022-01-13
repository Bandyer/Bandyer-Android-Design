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

package com.bandyer.sdk_design.screensharing

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.dp2px
import com.bandyer.sdk_design.extensions.getScreenSize
import com.bandyer.sdk_design.extensions.scanForFragmentActivity
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutObserver
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutOffsetListener

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
class StatusBarOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SystemViewLayoutObserver, Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private val backgroundColor = ContextCompat.getColor(context, R.color.bandyer_screen_share_color)
    private val safeViewHeightRange = with(context) { dp2px(25f) until dp2px(55f) }
    private val pictureInPictureHeight = context.dp2px(10f)

    init {
        id = R.id.bandyer_id_screen_share_overlay
    }

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.addObserver(it, this)
        }
        addAlphaAnimationListeners(this, this)
        updateHeight()
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.removeObserver(it, this)
        }
        removeAlphaAnimationListener(this, this)
    }

    /**
     * @suppress
     */
    override fun onTopInsetChanged(pixels: Int) {
        if (isInPictureInPictureMode()) return
        statusBarHeight = pixels.coerceIn(safeViewHeightRange)
        updateHeight()
    }

    private fun updateHeight(height: Int = statusBarHeight) {
        if (layoutParams == null || layoutParams.height == height) return
        val layoutParams = layoutParams
        layoutParams.height = height
        this.layoutParams = layoutParams
        bringToFront()
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
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (!isInPictureInPictureMode()) return
        val multiplier = (width).toFloat() / context.getScreenSize().x
        if (multiplier > 1) return
        updateHeight(pictureInPictureHeight)
    }

    private fun isInPictureInPictureMode(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        with(context as? AppCompatActivity) {
            this ?: false
            kotlin.runCatching { this?.isInPictureInPictureMode }.getOrNull() ?: false
        }
    } else false

    /**
     * @suppress
     */
    override fun onAnimationRepeat(animation: Animator?) = bringToFront()

    /**
     * @suppress
     */
    override fun onAnimationStart(animation: Animator?) = bringToFront()

    /**
     * @suppress
     */
    override fun onAnimationEnd(animation: Animator?) = Unit

    /**
     * @suppress
     */
    override fun onAnimationCancel(animation: Animator?) = setBackgroundColor(backgroundColor)

    /**
     * @suppress
     */
    override fun onAnimationUpdate(animation: ValueAnimator?) {
        animation ?: return
        // Use animation position to blend colors.
        val position = animation.animatedFraction
        val blended = blendColors(Color.TRANSPARENT, backgroundColor, position)
        // Apply blended color to the view.
        setBackgroundColor(blended)
    }

    /**
     * Instance of StatusBarOverlayView
     */
    companion object {
        private var statusBarHeight = 0

        /**
         * Adds animator listener and update listener to the status bar global animation,
         * so the animation is synchronized across all status bar overlay view instances
         * @param animatorListener AnimatorListener animator listener
         * @param animatorUpdateListener AnimatorUpdateListener animator update listener
         */
        fun addAlphaAnimationListeners(animatorListener: Animator.AnimatorListener, animatorUpdateListener: ValueAnimator.AnimatorUpdateListener) {
            alphaAnimation.addListener(animatorListener)
            alphaAnimation.addUpdateListener(animatorUpdateListener)
            if (!alphaAnimation.isRunning) alphaAnimation.start()
        }

        /**
         * Removes animator listener and update listener from the status bar global animation
         * @param animatorListener AnimatorListener animator listener
         * @param animatorUpdateListener AnimatorUpdateListener animator update listener
         */
        fun removeAlphaAnimationListener(animatorListener: Animator.AnimatorListener, animatorUpdateListener: ValueAnimator.AnimatorUpdateListener) {
            alphaAnimation.removeListener(animatorListener)
            alphaAnimation.removeUpdateListener(animatorUpdateListener)
            if (alphaAnimation.listeners.isNullOrEmpty()) alphaAnimation.cancel()
        }

        private val alphaAnimation: ValueAnimator by lazy {
            ValueAnimator.ofFloat(0f, 1f).apply {
                this.repeatMode = ValueAnimator.REVERSE
                this.repeatCount = ValueAnimator.INFINITE
                this.duration = 2000L
            }
        }
    }
}