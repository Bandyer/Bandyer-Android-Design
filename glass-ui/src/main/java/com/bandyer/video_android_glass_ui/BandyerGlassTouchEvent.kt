package com.bandyer.video_android_glass_ui

import android.view.KeyEvent
import com.bandyer.video_android_glass_ui.utils.GlassGestureDetector
import com.bandyer.video_android_glass_ui.utils.extensions.isConfirmButton
import com.bandyer.video_android_glass_ui.utils.extensions.isDownButton
import com.bandyer.video_android_glass_ui.utils.extensions.isLeftButton
import com.bandyer.video_android_glass_ui.utils.extensions.isRightButton

/**
 * A smart glass touch event
 *
 * @property type The event type
 * @property source The event source
 * @constructor
 */
data class BandyerGlassTouchEvent(val type: Type, val source: Source) {
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
                GlassGestureDetector.Gesture.TAP            -> BandyerGlassTouchEvent(Type.TAP, Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_DOWN     -> BandyerGlassTouchEvent(
                    Type.SWIPE_DOWN,
                    Source.GESTURE
                )
                GlassGestureDetector.Gesture.SWIPE_FORWARD  -> BandyerGlassTouchEvent(
                    Type.SWIPE_FORWARD,
                    Source.GESTURE
                )
                GlassGestureDetector.Gesture.SWIPE_BACKWARD -> BandyerGlassTouchEvent(
                    Type.SWIPE_BACKWARD,
                    Source.GESTURE
                )
                else                                        -> BandyerGlassTouchEvent(Type.UNKNOWN, Source.GESTURE)
            }

        /**
         * Utility function to map a KeyEvent to a SmartGlassTouchEvent
         *
         * @param event The key event
         * @return SmartGlassTouchEvent
         */
        fun getEvent(event: KeyEvent?) =
            when {
                event?.isConfirmButton() == true -> BandyerGlassTouchEvent(Type.TAP, Source.KEY)
                event?.isDownButton() == true -> BandyerGlassTouchEvent(
                    Type.SWIPE_DOWN,
                    Source.KEY
                )
                event?.isRightButton() == true -> BandyerGlassTouchEvent(
                    Type.SWIPE_FORWARD,
                    Source.KEY
                )
                event?.isLeftButton() == true -> BandyerGlassTouchEvent(
                    Type.SWIPE_BACKWARD,
                    Source.KEY
                )
                else -> BandyerGlassTouchEvent(Type.UNKNOWN, Source.KEY)
            }
    }
}