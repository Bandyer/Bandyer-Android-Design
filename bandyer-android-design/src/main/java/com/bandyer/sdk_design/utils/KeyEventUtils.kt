package com.bandyer.sdk_design.utils

import android.view.KeyEvent

fun KeyEvent.isConfirmButton(): Boolean = keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                keyCode == android.view.KeyEvent.KEYCODE_SPACE