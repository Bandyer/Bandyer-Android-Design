package com.kaleyra.video_common_ui.utils

import android.os.Build

object UsbCameraUtils {

    fun isSupported(): Boolean = Build.VERSION.SDK_INT != Build.VERSION_CODES.Q
}