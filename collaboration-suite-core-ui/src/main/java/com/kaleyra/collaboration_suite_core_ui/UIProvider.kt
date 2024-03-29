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

package com.kaleyra.collaboration_suite_core_ui

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils.isSmartGlass
import com.kaleyra.collaboration_suite_utils.ContextRetainer

internal object UIProvider {

    fun showCall(activityClazz: Class<*>) =
        with(ContextRetainer.context) {
            val intent = Intent(this, activityClazz).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("enableTilt", isSmartGlass)
            }
            startActivity(intent)
        }

    fun showChat(context: Context, activityClazz: Class<*>, userId: String) =
        with(context) {
            val intent = Intent(this, activityClazz).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("enableTilt", isSmartGlass)
//                putExtra("chatId", chatId)
                putExtra("userId", userId)
            }
            startActivity(intent)
        }
}