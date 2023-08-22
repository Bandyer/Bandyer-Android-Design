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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.color.MaterialColors
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_utils.HostAppInfo

/**
 * ChatNotification
 */
internal class ChatNotification {

    /**
     * @suppress
     */
    companion object {
        /**
         * The reply action extra
         */
        const val EXTRA_REPLY = "com.kaleyra.collaboration_suite_core_ui.EXTRA_REPLY"
    }

    /**
     * Builder
     *
     * @property context The context used to construct the notification
     * @property channelId The notification channel id
     * @property channelName The notification channel name showed to the users
     * @property username The local user name
     * @property userId The local user id
     * @property avatar The local user avatar
     * @property contentTitle The notification title
     * @property messages The message list
     * @property isGroupChat True if it's a group chat notification. False otherwise
     * @property contentIntent The pending intent to be executed when the user tap on the notification
     * @property replyIntent The pending intent to be executed when the user taps the reply button
     * @property markAsReadIntent The pending intent to be executed when the user taps the mark as read button
     * @property fullscreenIntent The pending intent to be executed when notification is in the lock screen
     * @constructor
     */
    data class Builder(
        val context: Context,
        val channelId: String,
        val channelName: String,
        var username: String = "",
        var userId: String = "",
        var avatar: Uri = Uri.EMPTY,
        var contentTitle: String = "",
        var messages: List<ChatNotificationMessage> = listOf(),
        var isGroupChat: Boolean = false,
        var contentIntent: PendingIntent? = null,
        var replyIntent: PendingIntent? = null,
        var markAsReadIntent: PendingIntent? = null,
//        var deleteIntent: PendingIntent? = null,
        var fullscreenIntent: PendingIntent? = null
    ) {
        /**
         * Set the username
         *
         * @param text The user name
         * @return Builder
         */
        fun username(text: String) = apply { this.username = text }

        /**
         * Set the user id
         *
         * @param text The user id
         * @return Builder
         */
        fun userId(text: String) = apply { this.userId = text }

        /**
         * Set the user avatar
         *
         * @param uri The avatar Uri
         * @return Builder
         */
        fun avatar(uri: Uri) = apply { this.avatar = uri }

        /**
         * Set notification title
         *
         * @param text The title
         * @return Builder
         */
        fun contentTitle(text: String) = apply { this.contentTitle = text }

        /**
         * Set notification messages
         *
         * @param list The notification message list
         * @return Builder
         */
        fun messages(list: List<ChatNotificationMessage>) = apply { this.messages = list }

        /**
         * Set the group chat flag
         *
         * @param value True to set the notification as a group chat notification, false otherwise
         * @return Builder
         */
        fun isGroupChat(value: Boolean) = apply { this.isGroupChat = value }

        /**
         * The pending intent to be executed when the user tap on the notification
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun contentIntent(pendingIntent: PendingIntent) =
            apply { this.contentIntent = pendingIntent }

        /**
         * The pending intent to be executed when the user taps the reply button
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun replyIntent(pendingIntent: PendingIntent) =
            apply { this.replyIntent = pendingIntent }

        /**
         * The pending intent to be executed when the user taps the mark as read button
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun markAsReadIntent(pendingIntent: PendingIntent) =
            apply { this.markAsReadIntent = pendingIntent }

//        fun deleteIntent(pendingIntent: PendingIntent) = apply { this.deleteIntent = pendingIntent }

        /**
         * The pending intent to be executed when notification is in the lock screen
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun fullscreenIntent(pendingIntent: PendingIntent) =
            apply { this.fullscreenIntent = pendingIntent }

        /**
         * Build the chat notification
         *
         * @return Notification
         */
        fun build(): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(context, channelId, channelName)

            val applicationIcon =
                context.applicationContext.packageManager.getApplicationIcon(HostAppInfo.name)
            val person = Person.Builder()
                .setName(username.takeIf { it.isNotEmpty() } ?: " ")
                .setKey(userId)
                .build()

            val messagingStyle: NotificationCompat.MessagingStyle =
                /*
                 * This API's behavior was changed in SDK version P. If your application's target version is
                 * less than {@link Build.VERSION_CODES#P}, setting a conversation title to
                 * a non-null value will make {@link #isGroupConversation()} return
                 * {@code true} and passing {@code null} will make it return {@code false}.
                 * This behavior can be overridden by calling
                 * {@link #setGroupConversation(boolean)} regardless of SDK version.
                 * In {@code P} and above, this method does not affect group conversation
                 * settings.
                 */
                NotificationCompat.MessagingStyle(person)
                    .setConversationTitle(contentTitle)
                    .setGroupConversation(isGroupChat)

            messages.forEach {
                val participant = Person.Builder()
                    .setName(it.username.takeIf { it.isNotEmpty() } ?: " ")
                    .setKey(it.userId)
                    .build()
                val message = NotificationCompat.MessagingStyle.Message(
                    it.text,
                    it.timestamp,
                    participant
                )
                messagingStyle.addMessage(message)
            }

            val messageCount = messages.count()
            val builder =
                NotificationCompat.Builder(context.applicationContext, channelId)
                    .setStyle(messagingStyle)
                    .setSmallIcon(R.drawable.ic_kaleyra_chat)
                    .setLargeIcon(applicationIcon.toBitmap())
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    // Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
                    // devices and all Wear devices. If you have more than one notification and
                    // you prefer a different summary notification, set a group key and create a
                    // summary notification via
                    .setGroupSummary(true)
                    .setGroup(userId)
                    .setAutoCancel(true)
                    // Number of new notifications for API <24 (M and below) devices.
                    .setSubText("$messageCount")
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setNumber(messageCount)


            contentIntent?.also { builder.setContentIntent(it) }
            replyIntent?.also { builder.addAction(createReplyAction(context, it)) }
            markAsReadIntent?.also {
                val markAsReadAction = NotificationCompat.Action(
                    R.drawable.ic_kaleyra_mark_as_read,
                    context.getString(R.string.kaleyra_notification_chat_mark_as_read),
                    it
                )
                builder.addAction(markAsReadAction)
            }
//            deleteIntent?.also { builder.setDeleteIntent(it) }
            fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }

            MaterialColors
                .getColor(context, R.attr.colorSecondary, -1)
                .takeIf { it != -1 }?.also { builder.color = it }

            return builder.build()
        }

        private fun createReplyAction(
            context: Context,
            replyIntent: PendingIntent
        ): NotificationCompat.Action {
            val replyLabel = context.resources.getString(R.string.kaleyra_notification_chat_reply)
            val remoteInput = RemoteInput.Builder(EXTRA_REPLY)
                .setLabel(replyLabel)
                .build()

            return NotificationCompat.Action.Builder(
                R.drawable.ic_kaleyra_reply,
                replyLabel,
                replyIntent
            )
                .addRemoteInput(remoteInput)
                .setShowsUserInterface(false) // Informs system we aren't bringing up our own custom UI for a reply action
                .setAllowGeneratedReplies(true) // Allows system to generate replies by context of conversation
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .build()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(
            context: Context,
            channelId: String,
            channelName: String
        ) {
            val notificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}