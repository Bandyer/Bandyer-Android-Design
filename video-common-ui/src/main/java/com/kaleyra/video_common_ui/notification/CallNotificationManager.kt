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

package com.kaleyra.video_common_ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.PendingIntentExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isScreenLocked
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.turnOnScreen
import com.kaleyra.video_utils.ContextRetainer

/**
 * CallNotificationManager
 */
internal interface CallNotificationManager {

    companion object {
        private const val DEFAULT_CHANNEL_ID =
            "com.kaleyra.video_common_ui.call_notification_channel_default"
        private const val IMPORTANT_CHANNEL_ID =
            "com.kaleyra.video_common_ui.call_notification_channel_important"

        private const val FULL_SCREEN_REQUEST_CODE = 123
        private const val CONTENT_REQUEST_CODE = 456
        private const val ANSWER_REQUEST_CODE = 789
        private const val DECLINE_REQUEST_CODE = 987
        private const val SCREEN_SHARING_REQUEST_CODE = 654
    }

    /**
     * Utility function which builds an incoming notification
     *
     * @param username The callee/caller
     * @param isGroupCall True if the call is group call, false otherwise
     * @param activityClazz The call ui activity class
     * @param isHighPriority True to set the notification with high priority, false otherwise
     * @return Notification
     */
    fun buildIncomingCallNotification(
        username: String,
        isGroupCall: Boolean,
        activityClazz: Class<*>,
        isHighPriority: Boolean,
        enableCallStyle: Boolean
    ): Notification {
        val context = ContextRetainer.context

        if (isHighPriority && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            context.turnOnScreen()

        val userText = if (isGroupCall) context.resources.getString(R.string.kaleyra_notification_incoming_group_call) else username
        val tapToReturnText = context.getString(if (isGroupCall) R.string.kaleyra_notification_tap_to_return_to_group_call else R.string.kaleyra_notification_tap_to_return_to_call)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = if (isHighPriority) IMPORTANT_CHANNEL_ID else DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(if (isHighPriority) R.string.kaleyra_notification_call_channel_high_priority_name else R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.INCOMING
            )
            .user(userText)
            .importance(isHighPriority)
            .enableCallStyle(enableCallStyle)
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .apply {
                if (context.isScreenLocked() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) fullscreenIntent(fullScreenPendingIntent(context, activityClazz))
            }
            .answerIntent(answerPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    /**
     * Utility function which builds an outgoing notification
     *
     * @param username The callee/caller
     * @param isGroupCall True if the call is group call, false otherwise
     * @param activityClazz The call ui activity class
     * @return Notification
     */
    fun buildOutgoingCallNotification(
        username: String,
        isGroupCall: Boolean,
        activityClazz: Class<*>,
        enableCallStyle: Boolean
    ): Notification {
        val context = ContextRetainer.context
        val userText =
            if (isGroupCall) context.resources.getString(R.string.kaleyra_notification_outgoing_group_call) else username
        val tapToReturnText = context.getString(if (isGroupCall) R.string.kaleyra_notification_tap_to_return_to_group_call else R.string.kaleyra_notification_tap_to_return_to_call)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.OUTGOING
            )
            .user(userText)
            .enableCallStyle(enableCallStyle)
            .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) fullscreenIntent(fullScreenPendingIntent(context, activityClazz)) }
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    /**
     * Utility function which builds an ongoing notification
     * 
     * @param username The callee/caller
     * @param isGroupCall True if the call is group call, false otherwise
     * @param isCallRecorded True if the call is recorded, false otherwise
     * @param activityClazz The call ui activity class
     * @param isLink Boolean True if the call is a link call, false otherwise
     * @param isSharingScreen Boolean True if is currently sharing screen, false otherwise
     * @param isConnecting Boolean True if the call is connecting, false otherwise
     * @param enableCallStyle Boolean True if should enable call notification style, false otherwise
     * @return Notification
     */
    fun buildOngoingCallNotification(
        username: String,
        isLink: Boolean,
        isGroupCall: Boolean,
        isCallRecorded: Boolean,
        isSharingScreen: Boolean,
        isConnecting: Boolean,
        activityClazz: Class<*>,
        enableCallStyle: Boolean
    ): Notification {
        val context = ContextRetainer.context
        val userText =
            if (isGroupCall || isLink) context.resources.getString(if (isGroupCall) R.string.kaleyra_notification_ongoing_group_call else R.string.kaleyra_notification_ongoing_call) else username
        val contentText = context.resources.getString(
            when {
                isConnecting -> R.string.kaleyra_notification_connecting_call
                isCallRecorded -> R.string.kaleyra_notification_call_recorded
                else -> if (isGroupCall) R.string.kaleyra_notification_tap_to_return_to_group_call else R.string.kaleyra_notification_tap_to_return_to_call
            }
        )
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.ONGOING
            )
            .user(userText)
            .contentText(contentText)
            .enableCallStyle(enableCallStyle)
            .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) fullscreenIntent(fullScreenPendingIntent(context, activityClazz)) }
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))
            .timer(!isConnecting)
            .apply { if (isSharingScreen) screenShareIntent(screenSharePendingIntent(context)) }

        return builder.build()
    }

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE, activityClazz)

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(context, CONTENT_REQUEST_CODE, activityClazz)

    private fun answerPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(context, ANSWER_REQUEST_CODE, activityClazz, CallNotificationActionReceiver.ACTION_ANSWER)

    private fun declinePendingIntent(context: Context) =
        createBroadcastPendingIntent(
            context,
            DECLINE_REQUEST_CODE,
            CallNotificationActionReceiver.ACTION_HANGUP
        )

    private fun screenSharePendingIntent(context: Context) =
        createBroadcastPendingIntent(
            context,
            SCREEN_SHARING_REQUEST_CODE,
            CallNotificationActionReceiver.ACTION_STOP_SCREEN_SHARE
        )

    private fun <T> createCallActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        action: String? = null
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            // Setting main action and category launcher allows to open activity
            // from notification if there is already an instance, instead of a creating a new one
            this.action = Intent.ACTION_MAIN
            this.addCategory(Intent.CATEGORY_LAUNCHER)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.putExtra("enableTilt", DeviceUtils.isSmartGlass)
            action?.also { this.putExtra("notificationAction", it) }
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun createBroadcastPendingIntent(context: Context, requestCode: Int, action: String) =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, CallNotificationActionReceiver::class.java).apply {
                putExtra("notificationAction", action)
            },
            PendingIntentExtensions.updateFlags
        )
}