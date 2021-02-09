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
import android.graphics.Color
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.getScreenSize
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutObserver
import com.bandyer.sdk_design.utils.systemviews.implementation.SystemViewControlsAware

/**
 * Factory that is capable of creating a view that represents a pulsing red alert view to be placed
 * under the status bar. The view will be created with application context, it is safe to attach/detach it
 * across activities preventing context leaks.
 */
object StatusBarOverlayView : SystemViewLayoutObserver, View.OnAttachStateChangeListener, View.OnLayoutChangeListener {

    private const val STATUS_BAR_HEIGHT_LIMIT = 200

    private var view: View? = null

    private var statusBarHeight = 0

    private var systemControlsAware: SystemViewControlsAware? = null

    private var alphaAnimation: ValueAnimator? = null

    /**
     * Creates and returns the StatusBarOverlayView.
     * The view will adapt its height with the current status bar height.
     * @param activity FragmentActivity used in status bar height calculation process.
     * @return StatusBarOverlayView
     */
    fun create(activity: FragmentActivity): View {
        if (view != null) return view!!

        view = View(activity.applicationContext)
        view!!.id = R.id.bandyer_id_screen_share_overlay

        view!!.addOnAttachStateChangeListener(this)

        alphaAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
            this.repeatMode = ValueAnimator.REVERSE
            this.repeatCount = ValueAnimator.INFINITE
            this.duration = 2000L
            this.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) = view!!.bringToFront()
                override fun onAnimationStart(animation: Animator?) = view!!.bringToFront()
                override fun onAnimationEnd(animation: Animator?) = Unit
                override fun onAnimationCancel(animation: Animator?) = Unit
            })

            this.addUpdateListener { animation ->
                // Use animation position to blend colors.
                val position = animation.animatedFraction
                val blended = blendColors(Color.TRANSPARENT, Color.RED, position)
                // Apply blended color to the view.
                view!!.setBackgroundColor(blended)
            }
        }

        systemControlsAware = SystemViewControlsAware {}.bind(activity)
        systemControlsAware!!.addObserver(this@StatusBarOverlayView)
        systemControlsAware!!.getOffsets()
        return view!!
    }

    /**
     * Dispose all animation and StatusBarOverlayView's view observers used in layout calculations.
     */
    fun destroy() {
        systemControlsAware?.removeObserver(this@StatusBarOverlayView)
        view?.removeOnAttachStateChangeListener(this)
        view = null
        systemControlsAware = null
        alphaAnimation?.cancel()
        alphaAnimation = null
    }

    /**
     * @suppress
     */
    override fun onViewDetachedFromWindow(v: View?) {
        v ?: return
        systemControlsAware?.removeObserver(this@StatusBarOverlayView)
        alphaAnimation?.cancel()
        v.removeOnLayoutChangeListener(this)
    }

    /**
     * @suppress
     */
    override fun onViewAttachedToWindow(v: View?) {
        v ?: return
        alphaAnimation?.start()
        updateHeight()
        v.addOnLayoutChangeListener(this)
    }

    override fun onTopInsetChanged(pixels: Int) {
        if (pixels > STATUS_BAR_HEIGHT_LIMIT || pixels == 0) return
        statusBarHeight = pixels
        updateHeight()
    }

    private fun updateHeight(height: Int? = null) {
        view?.post {
            view?.layoutParams ?: return@post
            if (view!!.layoutParams.height == (height ?: statusBarHeight)) return@post
            view!!.layoutParams.height = height ?: statusBarHeight
            view!!.requestLayout()
            view!!.bringToFront()
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
        updateHeight((statusBarHeight * multiplier).toInt())
    }
}


