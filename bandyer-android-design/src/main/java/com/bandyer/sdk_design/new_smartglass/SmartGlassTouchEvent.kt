package com.bandyer.sdk_design.new_smartglass

import android.view.KeyEvent
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isConfirmButton
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isDownButton
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isLeftButton
import com.bandyer.sdk_design.new_smartglass.utils.extensions.isRightButton

/**
 * A smart glass touch event
 *
 * @property type The event type
 * @property source The event source
 * @constructor
 */
data class SmartGlassTouchEvent(val type: Type, val source: Source) {
    enum class Type {
        TAP,
        SWIPE_DOWN,
        SWIPE_FORWARD,
        SWIPE_BACKWARD,
        UNKNOWN
    }

    enum class Source {
        GESTURE,
        KEY
    }

    companion object {
        /**
         * Utility function to map a GlassGestureDetector.Gesture to a SmartGlassTouchEvent
         *
         * @param gesture The google glass gesture
         * @return SmartGlassTouchEvent
         */
        fun getEvent(gesture: GlassGestureDetector.Gesture?) =
            when (gesture) {
                GlassGestureDetector.Gesture.TAP -> SmartGlassTouchEvent(Type.TAP, Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_DOWN -> SmartGlassTouchEvent(
                    Type.SWIPE_DOWN,
                    Source.GESTURE
                )
                GlassGestureDetector.Gesture.SWIPE_FORWARD -> SmartGlassTouchEvent(
                    Type.SWIPE_FORWARD,
                    Source.GESTURE
                )
                GlassGestureDetector.Gesture.SWIPE_BACKWARD -> SmartGlassTouchEvent(
                    Type.SWIPE_BACKWARD,
                    Source.GESTURE
                )
                else -> SmartGlassTouchEvent(Type.UNKNOWN, Source.GESTURE)
            }

        /**
         * Utility function to map a KeyEvent to a SmartGlassTouchEvent
         *
         * @param event The key event
         * @return SmartGlassTouchEvent
         */
        fun getEvent(event: KeyEvent?) =
            when {
                event?.isConfirmButton() == true -> SmartGlassTouchEvent(Type.TAP, Source.KEY)
                event?.isDownButton() == true -> SmartGlassTouchEvent(
                    Type.SWIPE_DOWN,
                    Source.KEY
                )
                event?.isRightButton() == true -> SmartGlassTouchEvent(
                    Type.SWIPE_FORWARD,
                    Source.KEY
                )
                event?.isLeftButton() == true -> SmartGlassTouchEvent(
                    Type.SWIPE_BACKWARD,
                    Source.KEY
                )
                else -> SmartGlassTouchEvent(Type.UNKNOWN, Source.KEY)
            }
    }
}