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

package com.bandyer.sdk_design.utils

import android.os.Build

/**
 * Device representation
 * @property manufacturer string representation of device manufacturer
 * @property deviceName device name
 * @constructor
 */
internal open class AndroidDevice(val manufacturer: String, val deviceName: String) {
    /**
     * Current running Android Device
     */
    object CURRENT: AndroidDevice(Build.MANUFACTURER, Build.DEVICE)

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AndroidDevice) return false

        if (manufacturer != other.manufacturer) return false
        if (deviceName != other.deviceName) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = manufacturer.hashCode()
        result = 31 * result + deviceName.hashCode()
        return result
    }
}

/**
 * Checks if the current device is RealWear HMT-1 device
 * @return true if device is RealWear HMT-1, false otherwise
 */
internal fun AndroidDevice.CURRENT.isRealWearHTM1() = manufacturer == "RealWear inc." && deviceName == "HMT-1"

/**
 * Supported smart glasses listing
 */
internal object SupportedSmartGlasses {
    val list: List<AndroidDevice> = listOf(
        AndroidDevice("Google", "glass_v3"),
        AndroidDevice("RealWear inc.", "HMT-1"),
        AndroidDevice("vuzix", "m400"))
}