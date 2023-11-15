/*
 * Copyright (C) 2023 Kaleyra S.p.A. All Rights Reserved.
 * See LICENSE.txt for licensing information
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