package com.bandyer.video_android_glass_ui.common

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.HorizontalScrollView
import kotlin.math.abs

// TODO Add RTL support and onKeyEvent support
internal class HorizontalAutoScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private var mainHandler: Handler? = null
    private var animator: ObjectAnimator? = null

    private var lastScrollPosition = 0
    private val scrollStopMs = 100L
    private var scrollStopRunner: Runnable? = null

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
        performAutoScroll()
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
        if (ev != null && mainHandler != null && scrollStopRunner != null && animator != null) {
            when (ev.action) {
                MotionEvent.ACTION_UP -> mainHandler!!.postDelayed(scrollStopRunner!!, scrollStopMs)
                MotionEvent.ACTION_DOWN -> {
                    animator!!.cancel()
                    mainHandler!!.removeCallbacksAndMessages(null)
                }
            }
        }

        return super.onTouchEvent(ev)
    }

    private fun performAutoScroll() {
        animator?.cancel()
        mainHandler?.removeCallbacksAndMessages(null)
        val maxScroll = (getChildAt(0).width - width)
        val duration = maxScroll * 4L
        val target = if (abs(scrollX - maxScroll) < 5) 0 else maxScroll
        animator = ObjectAnimator.ofInt(this@HorizontalAutoScrollView, "scrollX", target).apply {
            this.duration = duration
            start()
        }
        mainHandler?.postDelayed({ performAutoScroll() }, duration)
    }
}