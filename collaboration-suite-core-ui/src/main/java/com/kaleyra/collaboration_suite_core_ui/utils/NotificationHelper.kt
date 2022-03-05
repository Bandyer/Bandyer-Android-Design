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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationReceiver

internal object NotificationHelper {

    private const val NOTIFICATION_DEFAULT_CHANNEL_ID =
        "com.kaleyra.collaboration_suite_glass_ui.notification_channel_default"
    private const val NOTIFICATION_IMPORTANT_CHANNEL_ID =
        "com.kaleyra.collaboration_suite_glass_ui.notification_channel_important"

    private const val FULL_SCREEN_REQUEST_CODE = 123
    private const val CONTENT_REQUEST_CODE = 456
    private const val ANSWER_REQUEST_CODE = 789
    private const val DECLINE_REQUEST_CODE = 987
    private const val HANGUP_REQUEST_CODE = 654

    fun <T> buildIncomingCallNotification(context: Context, usersDescription: String, isHighPriority: Boolean, activityClazz: Class<T>): Notification {
        val contextText = context.getString(R.string.kaleyra_notification_incoming_call)
        val fullScreenIntent = if(isHighPriority) createCallActivityPendingIntent(context.applicationContext, FULL_SCREEN_REQUEST_CODE, activityClazz) else null
        val contentIntent = createCallActivityPendingIntent(context.applicationContext, CONTENT_REQUEST_CODE, activityClazz)
        val answerAction = NotificationCompat.Action(
            R.drawable.kaleyra_z_audio_only,
            context.getString(R.string.kaleyra_notification_answer),
            createCallActivityPendingIntent(context.applicationContext, ANSWER_REQUEST_CODE, activityClazz, true)
        )
        val declineAction = NotificationCompat.Action(
            R.drawable.kaleyra_z_end_call,
            context.getString(R.string.kaleyra_notification_decline),
            createBroadcastPendingIntent(context, DECLINE_REQUEST_CODE, NotificationReceiver.ACTION_HANGUP)
        )

        return context.buildNotification(
            usersDescription = usersDescription,
            channelId = if(isHighPriority) NOTIFICATION_IMPORTANT_CHANNEL_ID else NOTIFICATION_DEFAULT_CHANNEL_ID,
            channelName = context.getString(R.string.kaleyra_notification_incoming_call),
            isHighPriority = isHighPriority,
            contentText = contextText,
            contentIntent = contentIntent,
            fullscreenIntent = fullScreenIntent,
            actions = listOf(answerAction, declineAction)
        )
    }

    fun <T> buildOngoingCallNotification(context: Context, usersDescription: String, activityClazz: Class<T>): Notification {
        val contentIntent = createCallActivityPendingIntent(context.applicationContext, CONTENT_REQUEST_CODE, activityClazz)
        val contentText = context.getString(R.string.kaleyra_notification_ongoing_call)
        val hangUpAction = NotificationCompat.Action(
            R.drawable.kaleyra_z_end_call,
            context.getString(R.string.kaleyra_notification_hangup),
            createBroadcastPendingIntent(context, HANGUP_REQUEST_CODE, NotificationReceiver.ACTION_HANGUP)
        )

        return context.buildNotification(
            usersDescription = usersDescription,
            channelId = NOTIFICATION_DEFAULT_CHANNEL_ID,
            channelName = context.getString(R.string.kaleyra_notification_ongoing_call),
            isHighPriority = false,
            contentText = contentText,
            contentIntent = contentIntent,
            actions = listOf(hangUpAction)
        )
    }

    fun cancelNotification(context: Context, notifyId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notifyId)
    }

    private fun createBroadcastPendingIntent(context: Context, requestCode: Int, action: String) =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, NotificationReceiver::class.java).apply {
                this.action = action
            },
            PendingIntentExtensions.updateFlags
        )

    private fun <T> createCallActivityPendingIntent(context: Context, requestCode: Int, activityClazz: Class<T>, enableAutoAnswer: Boolean = false): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("enableTilt", false)
            putExtra("autoAnswer", enableAutoAnswer)
        }
        return PendingIntent.getActivity(applicationContext, requestCode, intent, PendingIntentExtensions.updateFlags)
    }

    private fun Context.buildNotification(
        usersDescription: String,
        channelId: String,
        channelName: String,
        isHighPriority: Boolean,
        contentText: String,
        contentIntent: PendingIntent? = null,
        fullscreenIntent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = listOf()
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(channelId, channelName, isHighPriority)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.kaleyra_z_audio_only)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentText(contentText)
            .setContentTitle(usersDescription)

        contentIntent?.also { builder.setContentIntent(it) }
        fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }
        actions.forEach { builder.addAction(it) }

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Context.createNotificationChannel(
        channelId: String,
        channelName: String,
        isHighImportance: Boolean
    ) {
        val notificationManager =
            getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            if (isHighImportance) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }
}