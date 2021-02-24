package com.bandyer.sdk_design.utils

import android.view.KeyEvent

/**
 * Check if the input keycode represents a confirmation
 * @receiver KeyEvent the input keycode
 * @return Boolean true if represents a confirmation, false otherwise
 */
fun KeyEvent.isConfirmButton(): Boolean = keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                keyCode == android.view.KeyEvent.KEYCODE_SPACE