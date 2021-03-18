package com.bandyer.sdk_design.smartglass.call.menu.utils

import android.view.MotionEvent
import android.view.ViewGroup
import com.bandyer.android_common.FieldProperty

/**
 * Represents a view group whose motion events can be intercepted by a motion event interceptor
 * @param T
 */
interface MotionEventInterceptableViewGroup<T> where T : ViewGroup {

    fun dispatchMotionEventToInterceptor(event: MotionEvent?) = motionEventInterceptor?.onMotionEventIntercepted(event)
}

/**
 * Property specifying the motion event interceptor assigned to a MotionEventInterceptableViewGroup instance
 */
var <T> MotionEventInterceptableViewGroup<T>.motionEventInterceptor: MotionEventInterceptor? where T : ViewGroup by FieldProperty { null }
