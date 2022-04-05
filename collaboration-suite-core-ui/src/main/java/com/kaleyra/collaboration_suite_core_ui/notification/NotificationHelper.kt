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

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.turnOnScreen
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.ContextRetainer

internal object NotificationHelper {

    private const val NOTIFICATION_DEFAULT_CHANNEL_ID =
        "com.kaleyra.collaboration_suite_core_ui.notification_channel_default"
    private const val NOTIFICATION_IMPORTANT_CHANNEL_ID =
        "com.kaleyra.collaboration_suite_core_ui.notification_channel_important"

    private const val FULL_SCREEN_REQUEST_CODE = 123
    private const val CONTENT_REQUEST_CODE = 456
    private const val ANSWER_REQUEST_CODE = 789
    private const val DECLINE_REQUEST_CODE = 987

    fun notify(notificationId: Int, notification: Notification) {
        NotificationManagerCompat.from(ContextRetainer.context).notify(
            notificationId, notification
        )
    }

    fun cancelNotification(notificationId: Int) {
        val notificationManager =
            ContextRetainer.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    fun <T> buildIncomingCallNotification(
        user: String,
        isGroupCall: Boolean,
        activityClazz: Class<T>,
        isHighPriority: Boolean
    ): Notification {
        val context = ContextRetainer.context

        if (isHighPriority && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            context.turnOnScreen()

        val incomingCallText = context.resources.getString(R.string.kaleyra_notification_incoming_call)
        val tapToReturnText = context.getString(R.string.kaleyra_notification_tap_to_return)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = if (isHighPriority) NOTIFICATION_IMPORTANT_CHANNEL_ID else NOTIFICATION_DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(R.string.kaleyra_notification_channel_name),
                type = CallNotification.Type.INCOMING
            )
            .user(if (isGroupCall) incomingCallText else user)
            .importance(isHighPriority)
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .fullscreenIntent(fullScreenPendingIntent(context, activityClazz))
            .answerIntent(answerPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    fun <T> buildOutgoingCallNotification(
        user: String,
        isGroupCall: Boolean,
        activityClazz: Class<T>,
    ): Notification {
        val context = ContextRetainer.context
        val outgoingCallText = context.resources.getString(R.string.kaleyra_notification_outgoing_call)
        val tapToReturnText = context.getString(R.string.kaleyra_notification_tap_to_return)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = NOTIFICATION_DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(R.string.kaleyra_notification_channel_name),
                type = CallNotification.Type.OUTGOING
            )
            .user(if (isGroupCall) outgoingCallText else user)
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    fun <T> buildOngoingCallNotification(
        user: String,
        isGroupCall: Boolean,
        isCallRecorded: Boolean,
        activityClazz: Class<T>,
    ): Notification {
        val context = ContextRetainer.context
        val ongoingCallText = context.resources.getString(R.string.kaleyra_notification_ongoing_call)
        val tapToReturnText = context.getString(R.string.kaleyra_notification_tap_to_return)
        val recordingText = context.getString(R.string.kaleyra_notification_call_recorded)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = NOTIFICATION_DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(R.string.kaleyra_notification_channel_name),
                type = CallNotification.Type.ONGOING
            )
            .user(if (isGroupCall) ongoingCallText else user)
            .contentText(if (isCallRecorded) recordingText else tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(
            context,
            FULL_SCREEN_REQUEST_CODE,
            activityClazz
        )

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(
            context,
            CONTENT_REQUEST_CODE,
            activityClazz
        )

    private fun answerPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(
            context,
            ANSWER_REQUEST_CODE,
            activityClazz,
            true
        )

    private fun <T> createCallActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        enableAutoAnswer: Boolean = false
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("enableTilt", DeviceUtils.isSmartGlass)
            putExtra("autoAnswer", enableAutoAnswer)
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun declinePendingIntent(context: Context) =
        PendingIntent.getBroadcast(
            context,
            DECLINE_REQUEST_CODE,
            Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_HANGUP
            },
            PendingIntentExtensions.updateFlags
        )
}
