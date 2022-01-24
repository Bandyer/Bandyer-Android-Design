package com.bandyer.video_android_glass_ui.common

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

    private var mainHandler: Handler? = null
    private var animator: ObjectAnimator? = null
    private var lastTarget = 0

    private var lastScrollPosition = 0
    private val scrollStopMs = 100L
    private var scrollStopRunner: Runnable? = null

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