/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bandyer.video_android_glass_ui

import android.view.KeyEvent
import com.bandyer.video_android_glass_ui.utils.GlassGestureDetector
import com.bandyer.video_android_glass_ui.utils.extensions.*
import com.bandyer.video_android_glass_ui.utils.extensions.isConfirmButton
import com.bandyer.video_android_glass_ui.utils.extensions.isDownButton
import com.bandyer.video_android_glass_ui.utils.extensions.isLeftButton
import com.bandyer.video_android_glass_ui.utils.extensions.isRightButton
import android.view.KeyEvent.META_SHIFT_LEFT_ON
import android.view.KeyEvent.META_SHIFT_ON

/**
 * A smart glass touch event
 *
 * @property type The event type
 * @property source The event source
 * @constructor
 */
internal data class TouchEvent(val type: Type, val source: Source) {

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

    /**
     * EventMapper
     */
    companion object EventMapper {
        /**
         * Utility function to map a GlassGestureDetector.Gesture to a SmartGlassTouchEvent
         *
         * @param gesture The google glass gesture
         * @return SmartGlassTouchEvent
         */
        fun getEvent(gesture: GlassGestureDetector.Gesture?) =
            when (gesture) {
                GlassGestureDetector.Gesture.TAP            -> TouchEvent(Type.TAP, Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_DOWN     -> TouchEvent(Type.SWIPE_DOWN, Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_FORWARD  -> TouchEvent(Type.SWIPE_FORWARD, Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_BACKWARD -> TouchEvent(Type.SWIPE_BACKWARD, Source.GESTURE)
                else                                        -> TouchEvent(Type.UNKNOWN, Source.GESTURE)
            }

        /**
         * Utility function to map a KeyEvent to a SmartGlassTouchEvent
         *
         * @param event The key event
         * @return SmartGlassTouchEvent
         */
        fun getEvent(event: KeyEvent?) =
            when {
                event?.isConfirmButton() == true                                                                                        -> TouchEvent(Type.TAP, Source.KEY)
                event?.isDownButton() == true                                                                                           -> TouchEvent(Type.SWIPE_DOWN, Source.KEY)
                event?.isRightButton() == true || (event?.isTabKey() == true && event.metaState != META_SHIFT_ON or META_SHIFT_LEFT_ON) -> TouchEvent(Type.SWIPE_FORWARD, Source.KEY)
                event?.isLeftButton() == true || event?.isShiftLeftKey() == true                                                        -> TouchEvent(Type.SWIPE_BACKWARD, Source.KEY)
                else                                                                                                                    -> TouchEvent(Type.UNKNOWN, Source.KEY)
            }
    }
}