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

package com.kaleyra.demo_collaboration_suite_ui

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotification
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.demo_collaboration_suite_ui.databinding.ActivityCallNotificationBinding

class CallNotificationActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_CHANNEL_ID =
            "com.kaleyra.collaboration_suite_core_ui.call_notification_channel_default"
        private const val IMPORTANT_CHANNEL_ID =
            "com.kaleyra.collaboration_suite_core_ui.call_notification_channel_important"

        private const val FULL_SCREEN_REQUEST_CODE = 123
        private const val CONTENT_REQUEST_CODE = 456
        private const val ANSWER_REQUEST_CODE = 789
        private const val DECLINE_REQUEST_CODE = 987
        private const val SCREEN_SHARING_REQUEST_CODE = 654
    }

    val updateFlags = PendingIntent.FLAG_UPDATE_CURRENT.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) it or PendingIntent.FLAG_IMMUTABLE
        else it
    }

    private lateinit var binding: ActivityCallNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        binding.incomingButton.setOnClickListener {
            val notification = buildIncomingCallNotification("Mario", false, this::class.java, true)
            notificationManager.notify(1, notification)
        }

        binding.ongoingButton.setOnClickListener {
            val notification = buildOngoingCallNotification("Mario", false, false, false, false, false, this::class.java)
            notificationManager.notify(2, notification)
        }

        binding.outgoingButton.setOnClickListener {
            val notification = buildOutgoingCallNotification("Mario", false, this::class.java)
            notificationManager.notify(3, notification)
        }

        binding.cleanButton.setOnClickListener {
            notificationManager.cancelAll()
        }
    }

    private fun buildIncomingCallNotification(
        username: String,
        isGroupCall: Boolean,
        activityClazz: Class<*>,
        isHighPriority: Boolean
    ): Notification {
        val context = ContextRetainer.context

        val userText = if (isGroupCall) context.resources.getString(com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_incoming_call) else username
        val tapToReturnText = context.getString(if (isGroupCall) com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_tap_to_return_to_group_call else com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_tap_to_return_to_call)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = if (isHighPriority) IMPORTANT_CHANNEL_ID else DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(if (isHighPriority) com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_call_channel_high_priority_name else com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.INCOMING
            )
            .user(userText)
            .importance(isHighPriority)
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .fullscreenIntent(fullScreenPendingIntent(context, activityClazz))
            .answerIntent(answerPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    private fun buildOutgoingCallNotification(
        username: String,
        isGroupCall: Boolean,
        activityClazz: Class<*>,
    ): Notification {
        val context = ContextRetainer.context
        val userText =
            if (isGroupCall) context.resources.getString(com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_outgoing_call) else username
        val tapToReturnText = context.getString(if (isGroupCall) com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_tap_to_return_to_group_call else com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_tap_to_return_to_call)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.OUTGOING
            )
            .user(userText)
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    private fun buildOngoingCallNotification(
        username: String,
        isLink: Boolean,
        isGroupCall: Boolean,
        isCallRecorded: Boolean,
        isSharingScreen: Boolean,
        isConnecting: Boolean,
        activityClazz: Class<*>,
    ): Notification {
        val context = ContextRetainer.context
        val userText =
            if (isGroupCall || isLink) context.resources.getString(com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_ongoing_call) else username
        val contentText = context.resources.getString(
            when {
                isConnecting   -> com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_connecting_call
                isCallRecorded -> com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_call_recorded
                else           -> if (isGroupCall) com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_tap_to_return_to_group_call else com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_tap_to_return_to_call
            }
        )
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(com.kaleyra.collaboration_suite_core_ui.R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.ONGOING
            )
            .user(userText)
            .contentText(contentText)
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
            this.action = Intent.ACTION_MAIN
            this.addCategory(Intent.CATEGORY_LAUNCHER)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.putExtra("enableTilt", DeviceUtils.isSmartGlass)
            action?.also { this.putExtra("action", it) }
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            updateFlags
        )
    }

    private fun createBroadcastPendingIntent(context: Context, requestCode: Int, action: String) =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, CallNotificationActionReceiver::class.java).apply {
                this.action = action
            },
            updateFlags
        )
}