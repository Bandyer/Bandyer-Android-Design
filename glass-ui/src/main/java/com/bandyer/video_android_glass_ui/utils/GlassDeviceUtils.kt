/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bandyer.video_android_glass_ui.utils

import android.os.Build

internal object GlassDeviceUtils {

    val isGoogleGlass by lazy { Build.MANUFACTURER == "Google" && Build.DEVICE == "glass_v3" }
    val isVuzix by lazy { Build.MANUFACTURER == "vuzix" }
    val isRealWear by lazy { Build.MANUFACTURER == "RealWear inc." }
    val isMoverio by lazy { Build.MANUFACTURER == "EPSON" }

    val isSmartGlass by lazy { isGoogleGlass || isVuzix || isRealWear || isMoverio }
    val isSmartGlassWithDpad by lazy { isGoogleGlass || isVuzix || isMoverio }
}