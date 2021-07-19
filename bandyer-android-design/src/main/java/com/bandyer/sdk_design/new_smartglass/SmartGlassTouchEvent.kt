package com.bandyer.sdk_design.new_smartglass

import android.view.KeyEvent
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isConfirmButton
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isDownButton

object SmartGlassTouchEvent {

    enum class Event {
        TAP,
        SWIPE_DOWN,
        UNKNOWN
    }

    fun getEvent(gesture: GlassGestureDetector.Gesture?) =
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> Event.TAP
            GlassGestureDetector.Gesture.SWIPE_DOWN -> Event.SWIPE_DOWN
            else -> Event.UNKNOWN
        }

    fun getEvent(keyCode: Int, event: KeyEvent?) =
        when {
            event?.isConfirmButton() == true -> Event.TAP
            event?.isDownButton() == true -> Event.SWIPE_DOWN
            else -> Event.UNKNOWN
        }
}