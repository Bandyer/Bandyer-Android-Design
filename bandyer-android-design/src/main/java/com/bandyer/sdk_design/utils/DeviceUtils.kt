package com.bandyer.sdk_design.utils

import android.os.Build

/**
 * Checks if the current device is Realware HMT-1 device
 * @return true if device is Realware HMT-1, false otherwise
 */
fun isRealWearHTM1(): Boolean = Build.DEVICE == "HMT-1" && Build.MANUFACTURER == "RealWear inc."
