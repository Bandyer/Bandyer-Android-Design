package com.bandyer.sdk_design.smartglass.call.menu.utils

import android.view.MotionEvent

/**
 * Represents a class capable of intercepting motion events
 */
interface MotionEventInterceptor {

    /**
     * Called when a motion event occurs
     * @param event MotionEvent? event occurred
     */
    fun onMotionEventIntercepted(event: MotionEvent?)
}