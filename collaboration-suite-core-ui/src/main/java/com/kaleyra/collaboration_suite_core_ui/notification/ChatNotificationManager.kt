package com.kaleyra.collaboration_suite_core_ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.ContextRetainer

interface ChatNotificationManager {

    companion object {
        private const val DEFAULT_CHANNEL_ID =
            "com.kaleyra.collaboration_suite_core_ui.chat_notification_channel_default"

        private const val FULL_SCREEN_REQUEST_CODE = 321
        private const val CONTENT_REQUEST_CODE = 654
        private const val REPLY_REQUEST_CODE = 987
        private const val DELETE_REQUEST_CODE = 1110
    }

    fun buildChatNotification(
        userId: String,
        username: String,
        avatar: Uri,
        isGroupChat: Boolean,
        messages: List<ChatNotificationMessage>,
        activityClazz: Class<*>,
        asActivity: Boolean
    ): Notification {
        val context = ContextRetainer.context

        val contentIntent = contentPendingIntent(context, activityClazz)
        // Pending intent =
        //      API <24 (M and below): activity so the lock-screen presents the auth challenge.
        //      API 24+ (N and above): this should be a Service or BroadcastReceiver.
        val replyIntent = replyPendingIntent(context) ?: contentIntent

        val builder = ChatNotification
            .Builder(
                context,
                DEFAULT_CHANNEL_ID,
                context.resources.getString(R.string.kaleyra_notification_chat_channel_name)
            )
            .userId(userId)
            .username(username)
            .avatar(avatar)
            .isGroupChat(isGroupChat)
            .contentIntent(contentIntent)
            .replyIntent(replyIntent)
            .deleteIntent(deletePendingIntent(context))
            .messages(messages)

        if (asActivity)
            builder.fullscreenIntent(fullScreenPendingIntent(context, activityClazz))

        return builder.build()
    }

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>) =
        createChatActivityPendingIntent(context, CONTENT_REQUEST_CODE, activityClazz)

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>) =
        createChatActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE, activityClazz)

    private fun <T> createChatActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun replyPendingIntent(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(
                context.applicationContext,
                ChatNotificationActionReceiver::class.java
            ).apply {
                action = ChatNotificationActionReceiver.ACTION_REPLY
            }
            PendingIntent.getBroadcast(
                context.applicationContext,
                REPLY_REQUEST_CODE,
                intent,
                PendingIntentExtensions.mutableFlags
            )
        } else null

    private fun deletePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ChatNotificationActionReceiver::class.java).apply {
            this.action = ChatNotificationActionReceiver.ACTION_DELETE
        }
        return PendingIntent.getBroadcast(
            context,
            DELETE_REQUEST_CODE,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }
}