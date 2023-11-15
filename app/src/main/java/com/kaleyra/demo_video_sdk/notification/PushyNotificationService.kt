/*
 * Copyright (C) 2023 Kaleyra S.p.A. All Rights Reserved.
 * See LICENSE.txt for licensing information
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