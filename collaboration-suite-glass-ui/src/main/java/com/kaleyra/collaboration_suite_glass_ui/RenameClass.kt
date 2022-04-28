package com.kaleyra.collaboration_suite_glass_ui

import android.view.KeyEvent
import android.view.MotionEvent
import com.kaleyra.collaboration_suite_glass_ui.utils.GlassGestureDetector

internal interface RenameClass : GlassGestureDetector.OnGestureListener {

    val glassGestureDetector: GlassGestureDetector

    val lastEventTime

    fun onGlassTouchEvent(glassEvent: TouchEvent): Boolean

    fun handleTouchEvent(event: MotionEvent?): Boolean =
        event != null && glassGestureDetector.onTouchEvent(event)

    fun handleKeyEvent(event: KeyEvent?): Boolean =
        event?.action == MotionEvent.ACTION_DOWN &&
                conflateGlassTouchEvent(TouchEvent.getEvent(event))

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        conflateGlassTouchEvent(TouchEvent.getEvent(gesture))

    private fun conflateGlassTouchEvent(glassEvent: TouchEvent): Boolean {

    }
}