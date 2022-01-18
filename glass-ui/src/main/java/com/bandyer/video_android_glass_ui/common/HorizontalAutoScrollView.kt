package com.bandyer.video_android_glass_ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

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


    private var autoScrollX = 5
    private var autoScrollRunnable = object : Runnable {
        override fun run() {
            val max = getChildAt(0).width - width
            target = when(scrollX) {
                max -> 0
                0 -> 1
                else -> target
            }

            if((target == 0 && autoScrollX > 0) || (target == 1 && autoScrollX < 0)) autoScrollX = -autoScrollX

            scrollBy(autoScrollX, 0)
            mainHandler.postDelayed(this, 1)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mainHandler.post(autoScrollRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainHandler.removeCallbacks(autoScrollRunnable)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        val result = super.onTouchEvent(ev)
        ev ?: return result
        when(ev.action) {
            MotionEvent.ACTION_UP -> mainHandler.post(autoScrollRunnable)
            MotionEvent.ACTION_DOWN -> mainHandler.removeCallbacks(autoScrollRunnable)
        }

        val currentEventTime = ev.eventTime
        if((currentEventTime - lastSampledEventTime) > samplePeriod) {
            val diff = ev.x - oldEventX
            if (diff > 0) target = 0 else if (diff < 0) target = 1
            oldEventX = ev.x
            lastSampledEventTime = currentEventTime
        }
        return result
    }
}