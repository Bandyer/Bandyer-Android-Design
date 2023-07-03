package com.kaleyra.collaboration_suite_core_ui.utils

import android.os.Build

object UsbCameraUtils {

    fun isSupported(): Boolean = Build.VERSION.SDK_INT != Build.VERSION_CODES.Q
}