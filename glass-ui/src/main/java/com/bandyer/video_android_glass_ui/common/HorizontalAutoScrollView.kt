package com.bandyer.video_android_glass_ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator


internal class HorizontalAutoScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private var mainHandler = Handler(Looper.getMainLooper())

    // add rtl support
    private var oldEventX = 0f
    private var target = 1

    private var lastSampledEventTime = 0L
    private var samplePeriod = 50

    private var animator: ObjectAnimator? = null
    private var autoScrollPx = 300
    private var autoScrollMs = 1000L

    private var autoScrollRunnable: Runnable? = object : Runnable {
        override fun run() {
            val max = getChildAt(0).width - width
            target = when (scrollX) {
                max -> 0
                0 -> 1
                else -> target
            }

            if ((target == 0 && autoScrollPx > 0) || (target == 1 && autoScrollPx < 0)) autoScrollPx = -autoScrollPx


            animator = ObjectAnimator.ofInt(this@HorizontalAutoScrollView, "scrollX", scrollX + autoScrollPx).apply {
                duration = autoScrollMs
                interpolator = LinearInterpolator()
                start()
            }
            mainHandler.postDelayed(this, autoScrollMs)
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mainHandler.post(autoScrollRunnable!!)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainHandler.removeCallbacksAndMessages(null)
        autoScrollRunnable = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if(ev != null && autoScrollRunnable != null && animator != null) {
            when (ev.action) {
                MotionEvent.ACTION_UP -> mainHandler.post(autoScrollRunnable!!)
                MotionEvent.ACTION_DOWN -> {
                    animator!!.cancel()
                    mainHandler.removeCallbacksAndMessages(null)
                }
            }

            val currentEventTime = ev.eventTime
            if ((currentEventTime - lastSampledEventTime) > samplePeriod) {
                val diff = ev.x - oldEventX
                if (diff > 0) target = 0 else if (diff < 0) target = 1
                oldEventX = ev.x
                lastSampledEventTime = currentEventTime
            }
        }

        return super.onTouchEvent(ev)
    }
}