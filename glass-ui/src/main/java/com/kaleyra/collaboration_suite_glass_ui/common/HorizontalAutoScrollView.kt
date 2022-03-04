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

package com.kaleyra.collaboration_suite_glass_ui.common

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import kotlin.math.abs

internal class HorizontalAutoScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    interface OnScrollListener {
        fun onScrollChanged(x: Int, y: Int)
    }

    private var mainHandler: Handler? = null
    private var animator: ObjectAnimator? = null
    private var lastTarget = 0

    private var lastScrollPosition = 0
    private val scrollStopMs = 100L
    private var scrollStopRunner: Runnable? = null

    var onScrollListener: OnScrollListener? = null

    init {
        post { performAutoScroll() }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        post { performAutoScroll() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mainHandler = Handler(Looper.getMainLooper())
        scrollStopRunner = object : Runnable {
            override fun run() {
                val currentScrollPosition = scrollX
                if (lastScrollPosition == currentScrollPosition) {
                    performAutoScroll()
                } else {
                    lastScrollPosition = currentScrollPosition
                    mainHandler?.postDelayed(this, scrollStopMs)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        mainHandler?.removeCallbacksAndMessages(null)
        animator = null
        scrollStopRunner = null
        mainHandler = null
        onScrollListener = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_UP -> scrollStopRunner?.apply { mainHandler?.postDelayed(this, scrollStopMs) }
            MotionEvent.ACTION_DOWN -> {
                animator?.cancel()
                mainHandler?.removeCallbacksAndMessages(null)
            }
        }

        return super.onTouchEvent(ev)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollListener?.onScrollChanged(l, t)
    }

    fun smoothScrollByWithAutoScroll(dx: Int, dy: Int) {
        animator?.cancel()
        mainHandler?.removeCallbacksAndMessages(null)
        smoothScrollBy(dx, dy)
        scrollStopRunner?.apply { mainHandler?.postDelayed(this, scrollStopMs) }
    }

    private fun performAutoScroll() {
        val diff = getChildAt(0).width - width
        if (diff <= 0) return
        animator?.cancel()
        mainHandler?.removeCallbacksAndMessages(null)
        val duration = diff * 4L
        val target = when {
            (abs(scrollX - diff) < 5) -> 0
            (scrollX < 5) -> diff
            else -> lastTarget
        }
        lastTarget = target
        animator = ObjectAnimator.ofInt(this@HorizontalAutoScrollView, "scrollX", target).apply {
            this.duration = duration
            start()
        }
        mainHandler?.postDelayed({ performAutoScroll() }, duration)
    }
}