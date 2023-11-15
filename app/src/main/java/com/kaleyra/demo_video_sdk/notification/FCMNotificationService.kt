/*
 * Copyright (C) 2023 Kaleyra S.p.A. All Rights Reserved.
 * See LICENSE.txt for licensing information
 */
package com.kaleyra.demo_video_sdk.notification

import android.util.Log
import androidx.work.Data.Builder
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kaleyra.app_utilities.notification.FirebaseCompat.registerDevice
import com.kaleyra.demo_video_sdk.notification.MissedNotificationPayloadWorker
import com.kaleyra.demo_video_sdk.notification.MissedNotificationPayloadWorker.Companion.isMissingCallMessage

/**
 * Sample implementation of a push notification receiver that handles incoming calls.
 * Push notification are not working in this sample and this class is intended to be used as a
 * sample snippet of code to be used when incoming call notification payloads are received through
 * your preferred push notification implementation.
 * The sample is based on Firebase implementation but can be easily applied to other
 * push notification libraries.
 */
class FCMNotificationService : FirebaseMessagingService() {
    /**
     * This function represent the push notification receive callback.
     * The incoming call payload must be extracted from the push notification.
     * The payload will be sent to WorkManager instance through PushNotificationPayloadWorker class to
     * ensure execution even if the app is killed by the system.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            val payload = remoteMessage.data["message"]
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

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        registerDevice(this)
    }

    companion object {
        private val TAG = FCMNotificationService::class.java.simpleName
    }
}