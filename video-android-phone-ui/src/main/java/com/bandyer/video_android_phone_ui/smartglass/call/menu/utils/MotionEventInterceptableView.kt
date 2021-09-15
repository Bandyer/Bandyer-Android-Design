package com.bandyer.video_android_phone_ui.smartglass.call.menu.utils

import android.view.MotionEvent
import android.view.View
import com.bandyer.android_common.FieldProperty

/**
 * Represents a view whose motion events can be intercepted by a motion event interceptor
 * @param T
 */
interface MotionEventInterceptableView<T> where T : View

/**
 * Property specifying the motion event interceptor assigned to a MotionEventInterceptableView instance
 */
var <T> MotionEventInterceptableView<T>.motionEventInterceptor: MotionEventInterceptor? where T : View by FieldProperty { null }

/**
 * Dispatches input motion events to the motionEventInterceptor if present
 * @receiver MotionEventInterceptableView<T> the MotionEventInterceptableView that should dispatch motion events to the motion event interceptor
 * @param event MotionEvent? optional input motion event that occurred on the view
 * @return Unit?
 */
fun <T> MotionEventInterceptableView<T>.dispatchMotionEventToInterceptor(event: MotionEvent?) where T : View = motionEventInterceptor?.onMotionEventIntercepted(event)
