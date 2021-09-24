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

    /**
     * Type of the touch event
     */
    enum class Type {

        /**
         * t a p
         */
        TAP,

        /**
         * s w i p e_d o w n
         */
        SWIPE_DOWN,

        /**
         * s w i p e_f o r w a r d
         */
        SWIPE_FORWARD,

        /**
         * s w i p e_b a c k w a r d
         */
        SWIPE_BACKWARD,

        /**
         * u n k n o w n
         */
        UNKNOWN
    }

    /**
     * Source of the touch event
     */
    enum class Source {

        /**
         * g e s t u r e
         */
        GESTURE,

        /**
         * k e y
         */
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