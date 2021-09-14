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
 * Checks if the current device is Realware HMT-1 device
 * @return true if device is Realware HMT-1, false otherwise
 */
internal fun isRealWearHTM1(): Boolean = Build.DEVICE == "HMT-1" && Build.MANUFACTURER == "RealWear inc."
