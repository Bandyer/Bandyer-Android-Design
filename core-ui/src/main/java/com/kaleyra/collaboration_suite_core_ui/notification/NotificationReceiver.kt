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

package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.call.CallService

internal class NotificationReceiver: BroadcastReceiver() {

    companion object {
        const val ACTION_ANSWER = "com.kaleyra.collaboration_suite_glass_ui.ANSWER"
        const val ACTION_HANGUP = "com.kaleyra.collaboration_suite_glass_ui.HANGUP"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return

        when (intent.action) {
            ACTION_ANSWER -> CallService.onNotificationAnswer()
            ACTION_HANGUP -> CallService.onNotificationHangUp()
            else -> Unit
        }
    }
}