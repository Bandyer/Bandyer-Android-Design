/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils

import android.os.Build

/**
 * Utility class to check if the device is a smartglass
 */
object DeviceUtils {

    /**
     * Flag. True if the device is a google glass, false otherwise
     */
    val isGoogleGlass by lazy { Build.MANUFACTURER == "Google" && Build.DEVICE == "glass_v3" }

    /**
     * Flag. True if the device is a vuzix, false otherwise
     */
    val isVuzix by lazy { Build.MANUFACTURER == "vuzix" }

    /**
     * Flag. True if the device is a realwear, false otherwise
     */
    val isRealWear by lazy { Build.MANUFACTURER == "RealWear inc." }

    /**
     * Flag. True if the device is an epson moverio, false otherwise
     */
    val isMoverio by lazy { Build.MANUFACTURER == "EPSON" }

    /**
     * Flag. True if the device is a smartglass, false otherwise
     */
    val isSmartGlass by lazy { isGoogleGlass || isVuzix || isRealWear || isMoverio }

    /**
     * Flag. True if the device is a smartglass with a dpad input, false otherwise
     */
    val isSmartGlassWithDpad by lazy { isGoogleGlass || isVuzix || isMoverio }
}
