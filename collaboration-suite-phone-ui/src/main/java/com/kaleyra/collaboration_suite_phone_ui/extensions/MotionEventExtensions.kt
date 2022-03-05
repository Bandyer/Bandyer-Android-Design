/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.extensions

import android.view.MotionEvent
import kotlin.math.abs

/**
 * Motion event extension utils
 */
object MotionEventExtensions {

    private const val MAX_CLICK_DURATION = 200
    private const val MAX_CLICK_DISTANCE = 5

    /**
     * Detect if a motion event represent a click event. This method should be called on an @see[MotionEvent.ACTION_UP] event
     *
     * @receiver MotionEvent
     * @param startX The x coordinate of the previous @see[MotionEvent.ACTION_DOWN] event
     * @param startY The y coordinate of the previous @see[MotionEvent.ACTION_DOWN] event
     * @return Boolean
     */
    fun MotionEvent.isClickEvent(startX: Float, startY: Float): Boolean =
        (eventTime - downTime) <= MAX_CLICK_DURATION && !(abs(startX - x) > MAX_CLICK_DISTANCE || abs(startY - y) > MAX_CLICK_DISTANCE)
}