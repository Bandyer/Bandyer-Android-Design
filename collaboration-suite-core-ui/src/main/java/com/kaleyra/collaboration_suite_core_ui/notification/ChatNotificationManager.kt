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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * The chat notification manager
 */
internal interface ChatNotificationManager {

    /**
     * @suppress
     */
    companion object {
        private const val DEFAULT_CHANNEL_ID =
            "com.kaleyra.collaboration_suite_core_ui.chat_notification_channel_default"

        private const val FULL_SCREEN_REQUEST_CODE = 321
        private const val CONTENT_REQUEST_CODE = 654
        private const val REPLY_REQUEST_CODE = 987
        private const val DELETE_REQUEST_CODE = 1110
        private const val MARK_AS_READ_REQUEST_CODE = 1312
    }

    fun cancelChatNotificationOnShow(scope: CoroutineScope) {
        DisplayedChatActivity.chatId
            .filter { it != null }
            .onEach { NotificationManager.cancel(it!!.hashCode()) }
            .launchIn(scope)
    }

    /**
     * Build the chat notification
     *
     * @param userId The user id
     * @param username The user name
     * @param avatar The user avatar
     * @param messages The list of messages
     * @param activityClazz The chat activity Class<*>
     * @param fullScreenIntentClazz The fullscreen intent activity Class<*>?
     * @return Notification
     */
    fun buildChatNotification(
        userId: String,
        username: String,
        avatar: Uri,
//        chatId: String,
        messages: List<ChatNotificationMessage>,
        activityClazz: Class<*>,
        fullScreenIntentClazz: Class<*>? = null,
    ): Notification {
        val context = ContextRetainer.context

        val otherUserId = messages.firstOrNull()?.userId ?: ""
        val contentIntent = contentPendingIntent(context, activityClazz, otherUserId)
        // Pending intent =
        //      API <24 (M and below): activity so the lock-screen presents the auth challenge.
        //      API 24+ (N and above): this should be a Service or BroadcastReceiver.
        val replyIntent = replyPendingIntent(context, otherUserId) ?: contentIntent

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
//            .replyIntent(replyIntent)
//            .markAsReadIntent(markAsReadIntent(context, otherUserId))
            .messages(messages)

        fullScreenIntentClazz?.let { builder.fullscreenIntent(fullScreenPendingIntent(context, it, otherUserId)) }
        return builder.build()
    }

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>, userId: String) =
        createChatActivityPendingIntent(context, CONTENT_REQUEST_CODE + userId.hashCode(), activityClazz, userId)

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>, userId: String) =
        createChatActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE + userId.hashCode(), activityClazz, userId)

    private fun <T> createChatActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        userId: String
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("userId", userId)
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun replyPendingIntent(context: Context, userId: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(
                context.applicationContext,
                ChatNotificationActionReceiver::class.java
            ).apply {
                action = ChatNotificationActionReceiver.ACTION_REPLY
                putExtra("userId", userId)
            }
            PendingIntent.getBroadcast(
                context.applicationContext,
                REPLY_REQUEST_CODE + userId.hashCode(),
                intent,
                PendingIntentExtensions.mutableFlags
            )
        } else null

    private fun markAsReadIntent(context: Context, userId: String): PendingIntent {
        val intent = Intent(context, ChatNotificationActionReceiver::class.java).apply {
            action = ChatNotificationActionReceiver.ACTION_MARK_AS_READ
            putExtra("userId", userId)
        }
        return createBroadcastPendingIntent(context, MARK_AS_READ_REQUEST_CODE + userId.hashCode(), intent)
    }

    private fun createBroadcastPendingIntent(context: Context, requestCode: Int, intent: Intent) =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
}