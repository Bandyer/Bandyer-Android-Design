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

package com.kaleyra.collaboration_suite_phone_ui.utils

import android.animation.Animator
import android.os.CountDownTimer
import android.view.View
import android.view.ViewPropertyAnimator

/**
 * Represents a view that can be hidden with an animation.
 */
interface ToggleableVisibilityInterface {

    /**
     * Toggle widget visibility
     * @param show true to show the widget, false otherwise
     * @param animationEndCallback callback that will be invoked when showing or hiding animation ends
     */
    fun toggleVisibility(show: Boolean, animationEndCallback: (shown: Boolean) -> Unit)

    /**
     * Cancels timer that hides automatically the widget
     */
    fun cancelTimer()
}

/**
 * Helper class that toggles a view's visibility animating with alpha.
 * @property view View the view to be toggled
 * @constructor
 */
class VisibilityToggle(val view: View): ToggleableVisibilityInterface {

    /**
     * Hide timer constants
     */
    companion object {
        /**
         * Autohide animation millis
         */
        const val AUTOHIDE_MS = 5300L
        /**
         * Show animnation millis
         */
        const val SHOW_MS = 300L
    }

    private var countDownTimer: CountDownTimer? = null
    private var alphaAnimator: ViewPropertyAnimator? = null

    override fun toggleVisibility(show: Boolean, animationEndCallback: (shown: Boolean) -> Unit) {
        val endValue = if (show) 1f else 0f
        if (view.alpha == endValue) return

        val animationListener = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) = Unit
            override fun onAnimationStart(animation: Animator) = Unit
            override fun onAnimationCancel(animation: Animator) {
                alphaAnimator = null
                cancelTimer()
            }
            override fun onAnimationEnd(p0: Animator) {
                alphaAnimator = null
                animationEndCallback.invoke(view.alpha == 1f)
            }
        }

        cancelTimer()

        alphaAnimator = view.animate()
                .alpha(endValue)
                .setDuration(SHOW_MS)
                .setListener(animationListener)

        alphaAnimator!!.start()

        countDownTimer = object : CountDownTimer(AUTOHIDE_MS, AUTOHIDE_MS) {
            override fun onFinish() {
                if (view.alpha != 1f) return
                alphaAnimator = view.animate().alpha(0f)
                        .setDuration(SHOW_MS)
                        .setListener(animationListener)

                alphaAnimator!!.start()
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }
        countDownTimer!!.start()
    }
    /**
     * Cancels timer that hides automatically the widget
     */
    override fun cancelTimer() {
        alphaAnimator?.cancel()
        countDownTimer?.cancel()
        countDownTimer = null
    }
}