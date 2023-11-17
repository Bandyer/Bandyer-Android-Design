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
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kaleyra.demo_video_sdk.DemoAppKaleyraVideoService

/**
 * Sample implementation of a worker object used to manage the push notification payload.
 * Using worker interface ensures that the payload parsing and process will be executed even if
 * the application is killed by the system.
 */
class PushNotificationPayloadWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        try {
            val payload = inputData.getString("payload") ?: return Result.failure()
            Log.d(TAG, "Received payload\n$payload.")
            DemoAppKaleyraVideoService.configure(applicationContext)
            DemoAppKaleyraVideoService.connect(applicationContext)
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.failure()
        }
        return Result.success()
    }

    companion object {
        private val TAG = PushNotificationPayloadWorker::class.java.simpleName
    }
}