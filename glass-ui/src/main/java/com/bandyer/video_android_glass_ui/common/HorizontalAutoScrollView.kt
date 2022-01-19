package com.bandyer.video_android_glass_ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.animation.ObjectAnimator
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import kotlin.math.abs

// TODO Add RTL support and onKeyEvent support
internal class HorizontalAutoScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr), ViewTreeObserver.OnScrollChangedListener {

    private var mainHandler: Handler? = Handler(Looper.getMainLooper())

    private var animator: ObjectAnimator? = null

    private var manual = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnScrollChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnScrollChangedListener(this)
        animator?.cancel()
        mainHandler?.removeCallbacksAndMessages(null)
        animator = null
        mainHandler = null
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!ViewCompat.isLaidOut(this)) return
        performAutoScroll()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_UP -> {
                manual = true
            }
            MotionEvent.ACTION_DOWN -> {
                animator?.cancel()
                mainHandler?.removeCallbacksAndMessages(null)
            }
        }

        return super.onTouchEvent(ev)
    }

    private fun performAutoScroll() {
        animator?.cancel()
        mainHandler?.removeCallbacksAndMessages(null)
        manual = false

        val maxScroll = (getChildAt(0).width - width)
        val duration = maxScroll * 4L
        val target = if (abs(scrollX - maxScroll) < 5) 0 else maxScroll
        animator = ObjectAnimator.ofInt(this@HorizontalAutoScrollView, "scrollX", target).apply {
            this.duration = duration
            start()
        }
        mainHandler?.postDelayed({ performAutoScroll() }, duration)
    }

    override fun onScrollChanged() {
        if (!manual) return
        mainHandler?.postDelayed({ performAutoScroll() }, 500)
    }
}