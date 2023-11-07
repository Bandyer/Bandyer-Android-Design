/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_glasses_sdk

import android.view.KeyEvent
import com.kaleyra.video_glasses_sdk.utils.GlassGestureDetector
import com.kaleyra.video_glasses_sdk.utils.extensions.*
import com.kaleyra.video_glasses_sdk.utils.extensions.isConfirmButton
import com.kaleyra.video_glasses_sdk.utils.extensions.isDownButton
import com.kaleyra.video_glasses_sdk.utils.extensions.isLeftButton
import com.kaleyra.video_glasses_sdk.utils.extensions.isRightButton
import android.view.KeyEvent.META_SHIFT_LEFT_ON
import android.view.KeyEvent.META_SHIFT_ON

/**
 * A smart glass touch event
 *
 * @property type The event type
 * @property source The event source
 * @constructor
 */
data class TouchEvent(val type: com.kaleyra.video_glasses_sdk.TouchEvent.Type, val source: com.kaleyra.video_glasses_sdk.TouchEvent.Source) {

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
                GlassGestureDetector.Gesture.TAP            -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.TAP, com.kaleyra.video_glasses_sdk.TouchEvent.Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_DOWN     -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.SWIPE_DOWN, com.kaleyra.video_glasses_sdk.TouchEvent.Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_FORWARD  -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.SWIPE_FORWARD, com.kaleyra.video_glasses_sdk.TouchEvent.Source.GESTURE)
                GlassGestureDetector.Gesture.SWIPE_BACKWARD -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.SWIPE_BACKWARD, com.kaleyra.video_glasses_sdk.TouchEvent.Source.GESTURE)
                else                                        -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.UNKNOWN, com.kaleyra.video_glasses_sdk.TouchEvent.Source.GESTURE)
            }

        /**
         * Utility function to map a KeyEvent to a SmartGlassTouchEvent
         *
         * @param event The key event
         * @return SmartGlassTouchEvent
         */
        fun getEvent(event: KeyEvent?) =
            when {
                event?.isConfirmButton() == true                                                                                        -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.TAP, com.kaleyra.video_glasses_sdk.TouchEvent.Source.KEY)
                event?.isDownButton() == true                                                                                           -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.SWIPE_DOWN, com.kaleyra.video_glasses_sdk.TouchEvent.Source.KEY)
                event?.isRightButton() == true || (event?.isTabKey() == true && event.metaState != META_SHIFT_ON or META_SHIFT_LEFT_ON) -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.SWIPE_FORWARD, com.kaleyra.video_glasses_sdk.TouchEvent.Source.KEY)
                event?.isLeftButton() == true || event?.isShiftLeftKey() == true                                                        -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.SWIPE_BACKWARD, com.kaleyra.video_glasses_sdk.TouchEvent.Source.KEY)
                else                                                                                                                    -> com.kaleyra.video_glasses_sdk.TouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.Type.UNKNOWN, com.kaleyra.video_glasses_sdk.TouchEvent.Source.KEY)
            }
    }
}