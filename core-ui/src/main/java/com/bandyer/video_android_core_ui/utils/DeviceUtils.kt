package com.bandyer.video_android_core_ui.utils

import android.os.Build

internal object DeviceUtils {

    val isGoogleGlass by lazy { Build.MANUFACTURER == "Google" && Build.DEVICE == "glass_v3" }
    val isVuzix by lazy { Build.MANUFACTURER == "vuzix" }
    val isRealWear by lazy { Build.MANUFACTURER == "RealWear inc." }
    val isMoverio by lazy { Build.MANUFACTURER == "EPSON" }

    val isSmartGlass by lazy { isGoogleGlass || isVuzix || isRealWear || isMoverio }
    val isSmartGlassWithDpad by lazy { isGoogleGlass || isVuzix || isMoverio }
}
