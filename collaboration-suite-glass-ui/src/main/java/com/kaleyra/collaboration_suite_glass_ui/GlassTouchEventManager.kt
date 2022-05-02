package com.kaleyra.collaboration_suite_glass_ui

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import com.kaleyra.collaboration_suite_glass_ui.utils.GlassGestureDetector

internal class GlassTouchEventManager(context: Context, private val listener: Listener) : GlassGestureDetector.OnGestureListener {

    interface Listener {
        fun onGlassTouchEvent(glassEvent: TouchEvent): Boolean
    }

    private var glassGestureDetector = GlassGestureDetector(context, this)

    private var lastTouchEventTime: Long = 0L

    fun toGlassTouchEvent(event: MotionEvent?): Boolean =
        event != null && glassGestureDetector.onTouchEvent(event)

    fun toGlassTouchEvent(event: KeyEvent?): Boolean =
        event?.action == MotionEvent.ACTION_DOWN &&
                backPressureGlassTouchEvent(TouchEvent.getEvent(event))

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        backPressureGlassTouchEvent(TouchEvent.getEvent(gesture))

    private fun backPressureGlassTouchEvent(glassEvent: TouchEvent): Boolean {
        val now = System.currentTimeMillis()
        if (now - TOUCH_EVENT_INTERVAL < lastTouchEventTime) return false
        lastTouchEventTime = now
        return listener.onGlassTouchEvent(glassEvent)
    }

    private companion object {
        const val TOUCH_EVENT_INTERVAL = 100L
    }
}