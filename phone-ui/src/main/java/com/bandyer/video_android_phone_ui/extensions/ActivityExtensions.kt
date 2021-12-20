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

package com.bandyer.video_android_phone_ui.extensions

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.getScreenSize

/**
 * Method to check if device is in Landscape or not
 */
fun Activity.isLandscape(): Boolean {
    val rotation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        display?.rotation
    } else {
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.rotation
    }
    return when (rotation) {
        Surface.ROTATION_0 -> false
        Surface.ROTATION_180 -> false
        else -> true
    }
}

/**
 * Hide keyboard
 * @receiver Activity
 * @param forced true if should be hidden no matter what, false otherwise
 */
fun Activity.hideKeyboard(forced: Boolean = false) {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val hideMethod = if (forced) 0 else InputMethodManager.HIDE_NOT_ALWAYS
    currentFocus?.let {
        inputManager.hideSoftInputFromWindow(it.windowToken, hideMethod)
    }
}

/**
 * Returns screen center coordinates as Pair<Float, Float>
 * @receiver Activity
 * @return Pair<Float, Float>
 */
fun Activity.getScreenCenter(): PointF {
    return PointF(getScreenSize().x / 2f, getScreenSize().y / 2f)
}

/**
 * Returns status bar height in pixels
 * @return value in pixels
 */
fun Activity.getStatusBarHeight(): Int {
    val rect = Rect()
    window.decorView.getWindowVisibleDisplayFrame(rect)
    val statusBarHeight = rect.top
    return if (statusBarHeight != getScreenSize().y) statusBarHeight else 0
}

/**
 * Returns navigation bar height in pixels
 * @return value in pixels
 */
fun Activity.getNavigationBarHeight(): Int {
    val rect = Rect()
    window.decorView.getWindowVisibleDisplayFrame(rect)
    return window.decorView.height - rect.bottom
}

/**
 * Finds next focus navigating with DPAD controller
 * @receiver Activity
 * @param keyCode Int direction
 * @param rootView ViewGroup root view
 * @return (android.view.View..android.view.View?) the next focused view
 */
fun Activity.findNextDPADFocus(keyCode: Int, rootView: ViewGroup): View? = FocusFinder.getInstance().findNextFocus(rootView, rootView.findFocus(), when (keyCode) {
    KeyEvent.KEYCODE_DPAD_LEFT -> {
        View.FOCUS_LEFT
    }
    KeyEvent.KEYCODE_DPAD_UP -> {
        View.FOCUS_UP
    }
    KeyEvent.KEYCODE_DPAD_RIGHT -> {
        View.FOCUS_RIGHT
    }
    KeyEvent.KEYCODE_DPAD_DOWN -> {
        View.FOCUS_DOWN
    }
    KeyEvent.KEYCODE_TAB -> {
        View.FOCUS_FORWARD
    }
    else -> {
        View.FOCUS_FORWARD
    }
})

/**
 * Check if activity is currently displayed in multi window mode.
 * @receiver Activity
 * @return Boolean
 */
fun Activity.checkIsInMultiWindowMode(): Boolean = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
    this.isInMultiWindowMode
} else false

/**
 * Check if activity is currently displayed in picture-in-picture mode.
 * @receiver Activity
 * @return Boolean
 */
fun Activity.checkIsInPictureInPictureMode(): Boolean = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
    this.isInPictureInPictureMode
} else false