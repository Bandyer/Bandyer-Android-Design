/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
