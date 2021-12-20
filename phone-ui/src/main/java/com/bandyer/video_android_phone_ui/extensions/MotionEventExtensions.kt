package com.bandyer.video_android_phone_ui.extensions

import android.view.MotionEvent
import kotlin.math.abs

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