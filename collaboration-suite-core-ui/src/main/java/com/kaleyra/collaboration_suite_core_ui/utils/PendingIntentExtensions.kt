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

package com.kaleyra.collaboration_suite_core_ui.utils

import android.app.PendingIntent
import android.os.Build

internal object PendingIntentExtensions {

    val updateFlags = PendingIntent.FLAG_UPDATE_CURRENT.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) it or PendingIntent.FLAG_IMMUTABLE
        else it
    }

    val mutableFlags = PendingIntent.FLAG_UPDATE_CURRENT.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it or PendingIntent.FLAG_MUTABLE
        else it
    }
}