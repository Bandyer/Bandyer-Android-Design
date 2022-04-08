package com.kaleyra.collaboration_suite_core_ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.turnOnScreen
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * CallNotificationManager
 */
internal interface CallNotificationManager {

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

    /**
     * Utility function which builds an incoming notification
     *
     * @param user The callee/caller
     * @param isGroupCall True if the call is group call, false otherwise
     * @param activityClazz The call ui activity class
     * @param isHighPriority True to set the notification with high priority, false otherwise
     * @return Notification
     */
    fun <T> buildIncomingCallNotification(
        user: String,
        isGroupCall: Boolean,
        activityClazz: Class<T>,
        isHighPriority: Boolean
    ): Notification {
        val context = ContextRetainer.context

        if (isHighPriority && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            context.turnOnScreen()

        val incomingCallText =
            context.resources.getString(R.string.kaleyra_notification_incoming_call)
        val tapToReturnText = context.getString(R.string.kaleyra_notification_tap_to_return)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = if (isHighPriority) IMPORTANT_CHANNEL_ID else DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(if (isHighPriority) R.string.kaleyra_notification_call_channel_high_priority_name else R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.INCOMING
            )
            .user(if (isGroupCall) incomingCallText else user)
            .importance(isHighPriority)
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .fullscreenIntent(fullScreenPendingIntent(context, activityClazz))
            .answerIntent(answerPendingIntent(context))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    /**
     * Utility function which builds an outgoing notification
     *
     * @param user The callee/caller
     * @param isGroupCall True if the call is group call, false otherwise
     * @param activityClazz The call ui activity class
     * @return Notification
     */
    fun <T> buildOutgoingCallNotification(
        user: String,
        isGroupCall: Boolean,
        activityClazz: Class<T>,
    ): Notification {
        val context = ContextRetainer.context
        val outgoingCallText =
            context.resources.getString(R.string.kaleyra_notification_outgoing_call)
        val tapToReturnText = context.getString(R.string.kaleyra_notification_tap_to_return)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.OUTGOING
            )
            .user(if (isGroupCall) outgoingCallText else user)
            .contentText(tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))

        return builder.build()
    }

    /**
     * Utility function which builds an ongoing notification
     *
     * @param user The callee/caller
     * @param isGroupCall True if the call is group call, false otherwise
     * @param isCallRecorded True if the call is recorded, false otherwise
     * @param activityClazz The call ui activity class
     * @return Notification
     */
    fun <T> buildOngoingCallNotification(
        user: String,
        isGroupCall: Boolean,
        isCallRecorded: Boolean,
        isSharingScreen: Boolean,
        activityClazz: Class<T>,
    ): Notification {
        val context = ContextRetainer.context
        val ongoingCallText =
            context.resources.getString(R.string.kaleyra_notification_ongoing_call)
        val tapToReturnText = context.getString(R.string.kaleyra_notification_tap_to_return)
        val recordingText = context.getString(R.string.kaleyra_notification_call_recorded)
        val builder = CallNotification
            .Builder(
                context = context,
                channelId = DEFAULT_CHANNEL_ID,
                channelName = context.resources.getString(R.string.kaleyra_notification_call_channel_low_priority_name),
                type = CallNotification.Type.ONGOING
            )
            .user(if (isGroupCall) ongoingCallText else user)
            .contentText(if (isCallRecorded) recordingText else tapToReturnText)
            .contentIntent(contentPendingIntent(context, activityClazz))
            .declineIntent(declinePendingIntent(context))
            .apply { if (isSharingScreen) screenShareIntent(screenSharePendingIntent(context)) }

        return builder.build()
    }

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE, activityClazz)

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>) =
        createCallActivityPendingIntent(context, CONTENT_REQUEST_CODE, activityClazz)

    private fun answerPendingIntent(context: Context) =
        createBroadcastPendingIntent(
            context,
            ANSWER_REQUEST_CODE,
            CallNotificationActionReceiver.ACTION_ANSWER
        )

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
        activityClazz: Class<T>
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("enableTilt", DeviceUtils.isSmartGlass)
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
                this.action = action
            },
            PendingIntentExtensions.updateFlags
        )
}