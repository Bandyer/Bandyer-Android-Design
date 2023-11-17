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
package com.kaleyra.demo_video_sdk.notification

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data.Builder
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.kaleyra.app_utilities.notification.PushyCompat
import com.kaleyra.demo_video_sdk.notification.MissedNotificationPayloadWorker.Companion.isMissingCallMessage

/**
 * @author kristiyan
 */
class PushyNotificationService : PushyCompat() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val payload = intent.getStringExtra("payload")
            Log.d(TAG, "Pushy payload received: $payload")
            val data = Builder()
                .putString("payload", payload)
                .build()

            val mRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(
                if (isMissingCallMessage(payload)) MissedNotificationPayloadWorker::class.java
                else PushNotificationPayloadWorker::class.java
            )
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(mRequest)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = PushyNotificationService::class.java.simpleName
    }
}