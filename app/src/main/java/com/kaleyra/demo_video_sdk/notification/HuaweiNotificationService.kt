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

import android.os.Bundle
import android.util.Log
import androidx.work.Data.Builder
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.kaleyra.app_utilities.notification.HuaweiCompat.registerDevice
import com.kaleyra.demo_video_sdk.notification.MissedNotificationPayloadWorker
import com.kaleyra.demo_video_sdk.notification.MissedNotificationPayloadWorker.Companion.isMissingCallMessage

class HuaweiNotificationService : HmsMessageService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            val payload = remoteMessage.data
            Log.d(TAG, "payload received: $payload")
            val data = Builder()
                .putString("payload", payload)
                .build()
            val mRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(
                if (isMissingCallMessage(payload)) MissedNotificationPayloadWorker::class.java
                else PushNotificationPayloadWorker::class.java
            )
                .setInputData(data)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(mRequest)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        registerDevice(this)
    }

    override fun onNewToken(s: String, bundle: Bundle) {
        super.onNewToken(s, bundle)
        registerDevice(this)
    }

    companion object {
        private val TAG = HuaweiNotificationService::class.java.simpleName
    }
}