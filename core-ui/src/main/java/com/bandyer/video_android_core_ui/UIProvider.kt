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

package com.bandyer.video_android_core_ui

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.bandyer.video_android_core_ui.utils.DeviceUtils.isSmartGlass

internal object UIProvider {

    fun <T> showCall(activityClazz: Class<T>) =
        with(ContextRetainer.context) {
            val intent = Intent(this, activityClazz).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                // TODO
                putExtra("enableTilt", isSmartGlass)
//            putExtra("options", listOf().toTypedArray())
            }
            startActivity(intent)
        }
}