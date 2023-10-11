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

package com.kaleyra.collaboration_suite_core_ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.video_utils.ContextRetainer
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
        private const val MARK_AS_READ_REQUEST_CODE = 1110
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
     * @param myUserId The user id
     * @param myUsername The user name
     * @param myAvatar The user avatar
     * @param messages The list of messages
     * @param activityClazz The chat activity Class<*>
     * @param fullScreenIntentClazz The fullscreen intent activity Class<*>?
     * @return Notification
     */
    fun buildChatNotification(
        myUserId: String,
        myUsername: String,
        myAvatar: Uri,
        chatId: String?,
        messages: List<ChatNotificationMessage>,
        activityClazz: Class<*>,
        fullScreenIntentClazz: Class<*>? = null,
    ): Notification {
        val context = ContextRetainer.context

        val otherUserIds = messages.map { it.userId }.distinct()
        val contentIntent = contentPendingIntent(context, activityClazz, otherUserIds, chatId)
        // Pending intent =
        //      API <24 (M and below): activity so the lock-screen presents the auth challenge.
        //      API 24+ (N and above): this should be a Service or BroadcastReceiver.
        val replyIntent = replyPendingIntent(context, otherUserIds, chatId) ?: contentIntent

        val builder = ChatNotification
            .Builder(
                context,
                DEFAULT_CHANNEL_ID,
                context.resources.getString(R.string.kaleyra_notification_chat_channel_name)
            )
            .userId(myUserId)
            .username(myUsername)
            .avatar(myAvatar)
            .isGroupChat(true) // Always true because of a notification ui bug
//            .isGroupChat(messages.map { it.userId }.distinct().count() > 1)
            .contentIntent(contentIntent)
//            .replyIntent(replyIntent)
//            .markAsReadIntent(markAsReadIntent(context, otherUserId))
            .messages(messages)

        fullScreenIntentClazz?.let { builder.fullscreenIntent(fullScreenPendingIntent(context, it, otherUserIds, chatId)) }
        return builder.build()
    }

    private fun contentPendingIntent(context: Context, activityClazz: Class<*>, userIds: List<String>, chatId: String?) =
        createChatActivityPendingIntent(context, CONTENT_REQUEST_CODE + userIds.hashCode(), activityClazz, userIds, chatId)

    private fun fullScreenPendingIntent(context: Context, activityClazz: Class<*>, userIds: List<String>, chatId: String?) =
        createChatActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE + userIds.hashCode(), activityClazz, userIds, chatId)

    private fun <T> createChatActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        userIds: List<String>,
        chatId: String?
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("userIds", userIds.toTypedArray())
            chatId?.let { putExtra("chatId", it) }
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun replyPendingIntent(context: Context, userIds: List<String>, chatId: String?) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(
                context.applicationContext,
                ChatNotificationActionReceiver::class.java
            ).apply {
                action = ChatNotificationActionReceiver.ACTION_REPLY
                putExtra("userIds", userIds.toTypedArray())
                chatId?.let { putExtra("chatId", it) }
            }
            PendingIntent.getBroadcast(
                context.applicationContext,
                REPLY_REQUEST_CODE + userIds.hashCode(),
                intent,
                PendingIntentExtensions.mutableFlags
            )
        } else null

    private fun markAsReadIntent(context: Context, userIds: String, chatId: String?): PendingIntent {
        val intent = Intent(context, ChatNotificationActionReceiver::class.java).apply {
            action = ChatNotificationActionReceiver.ACTION_MARK_AS_READ
            putExtra("userIds", userIds)
            chatId?.let { putExtra("chatId", it) }
        }
        return createBroadcastPendingIntent(context, MARK_AS_READ_REQUEST_CODE + userIds.hashCode(), intent)
    }

    private fun createBroadcastPendingIntent(context: Context, requestCode: Int, intent: Intent) =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
}