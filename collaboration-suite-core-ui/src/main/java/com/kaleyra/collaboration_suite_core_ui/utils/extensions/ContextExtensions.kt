package com.kaleyra.collaboration_suite_core_ui.utils.extensions

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display

object ContextExtensions {
    internal fun Context.isScreenOff(): Boolean = (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays.all { it.state != Display.STATE_ON }
}