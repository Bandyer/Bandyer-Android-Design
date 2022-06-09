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
        private const val MARK_AS_READ_REQUEST_CODE = 1312
    }

    fun buildChatNotification(
        userId: String,
        username: String,
        avatar: Uri,
        chatId: String,
        messages: List<ChatNotificationMessage>,
        activityClazz: Class<*>,
        asActivity: Boolean
    ): Notification {
        val context = ContextRetainer.context

        val contentIntent = contentPendingIntent(context, activityClazz, chatId)
        // Pending intent =
        //      API <24 (M and below): activity so the lock-screen presents the auth challenge.
        //      API 24+ (N and above): this should be a Service or BroadcastReceiver.
        val replyIntent = replyPendingIntent(context, chatId) ?: contentIntent

        val builder = ChatNotification
            .Builder(
                context,
                DEFAULT_CHANNEL_ID,
                context.resources.getString(R.string.kaleyra_notification_chat_channel_name)
            )
            .userId(userId)
            .username(username)
            .avatar(avatar)
            .isGroupChat(true) // Always true because of a notification ui bug
//            .isGroupChat(messages.map { it.userId }.distinct().count() > 1)
            .contentIntent(contentIntent)
            .replyIntent(replyIntent)
            .markAsReadIntent(markAsReadIntent(context, chatId))
//            .deleteIntent(deletePendingIntent(context))
            .messages(messages)

        if (asActivity)
            builder.fullscreenIntent(fullScreenPendingIntent(context, activityClazz, chatId))

        return builder.build()
    }

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>, chatId: String) =
        createChatActivityPendingIntent(context, CONTENT_REQUEST_CODE, activityClazz, chatId)

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>, chatId: String) =
        createChatActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE, activityClazz, chatId)

    private fun <T> createChatActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        chatId: String
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("chatId", chatId)
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun replyPendingIntent(context: Context, chatId: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(
                context.applicationContext,
                ChatNotificationActionReceiver::class.java
            ).apply {
                action = ChatNotificationActionReceiver.ACTION_REPLY
                putExtra("chatId", chatId)
            }
            PendingIntent.getBroadcast(
                context.applicationContext,
                REPLY_REQUEST_CODE,
                intent,
                PendingIntentExtensions.mutableFlags
            )
        } else null

    private fun markAsReadIntent(context: Context, chatId: String): PendingIntent {
        val intent = Intent(context, ChatNotificationActionReceiver::class.java).apply {
            action = ChatNotificationActionReceiver.ACTION_MARK_AS_READ
            putExtra("chatId", chatId)
        }
        return createBroadcastPendingIntent(context, MARK_AS_READ_REQUEST_CODE, intent)
    }

    private fun deletePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ChatNotificationActionReceiver::class.java).apply {
            action = ChatNotificationActionReceiver.ACTION_DELETE
        }
        return createBroadcastPendingIntent(context, DELETE_REQUEST_CODE, intent)
    }

    private fun createBroadcastPendingIntent(context: Context, requestCode: Int, intent: Intent) =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
}