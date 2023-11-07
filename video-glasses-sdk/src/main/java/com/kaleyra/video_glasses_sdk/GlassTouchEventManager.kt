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

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import com.kaleyra.video_glasses_sdk.utils.GlassGestureDetector

internal class GlassTouchEventManager(context: Context, private val listener: com.kaleyra.video_glasses_sdk.GlassTouchEventManager.Listener) : GlassGestureDetector.OnGestureListener {

    interface Listener {
        fun onGlassTouchEvent(glassEvent: com.kaleyra.video_glasses_sdk.TouchEvent): Boolean
    }

    private var glassGestureDetector = GlassGestureDetector(context, this)

    private var lastTouchEventTime: Long = 0L

    fun toGlassTouchEvent(event: MotionEvent?): Boolean =
        event != null && glassGestureDetector.onTouchEvent(event)

    fun toGlassTouchEvent(event: KeyEvent?): Boolean =
        event?.action == MotionEvent.ACTION_DOWN &&
                backPressureGlassTouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.EventMapper.getEvent(event))

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        backPressureGlassTouchEvent(com.kaleyra.video_glasses_sdk.TouchEvent.EventMapper.getEvent(gesture))

    private fun backPressureGlassTouchEvent(glassEvent: com.kaleyra.video_glasses_sdk.TouchEvent): Boolean {
        val now = System.currentTimeMillis()
        if (now - com.kaleyra.video_glasses_sdk.GlassTouchEventManager.Companion.TOUCH_EVENT_INTERVAL < lastTouchEventTime) return false
        lastTouchEventTime = now
        return listener.onGlassTouchEvent(glassEvent)
    }

    private companion object {
        const val TOUCH_EVENT_INTERVAL = 100L
    }
}