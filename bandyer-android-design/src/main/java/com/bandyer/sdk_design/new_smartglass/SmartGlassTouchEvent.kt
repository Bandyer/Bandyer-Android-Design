package com.bandyer.sdk_design.new_smartglass

import android.view.KeyEvent
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isConfirmButton
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isDownButton
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isLeftButton
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isRightButton

object SmartGlassTouchEvent {

    enum class Event {
        TAP,
        SWIPE_DOWN,
        SWIPE_FORWARD,
        SWIPE_BACKWARD,
        UNKNOWN
    }

    fun getEvent(gesture: GlassGestureDetector.Gesture?) =
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> Event.TAP
            GlassGestureDetector.Gesture.SWIPE_DOWN -> Event.SWIPE_DOWN
            GlassGestureDetector.Gesture.SWIPE_FORWARD -> Event.SWIPE_FORWARD
            GlassGestureDetector.Gesture.SWIPE_BACKWARD -> Event.SWIPE_BACKWARD
            else -> Event.UNKNOWN
        }

    fun getEvent(event: KeyEvent?) =
        when {
            event?.isConfirmButton() == true -> Event.TAP
            event?.isDownButton() == true -> Event.SWIPE_DOWN
            event?.isRightButton() == true -> Event.SWIPE_FORWARD
            event?.isLeftButton() == true -> Event.SWIPE_BACKWARD
            else -> Event.UNKNOWN
        }
}