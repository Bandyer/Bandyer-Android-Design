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
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.color.MaterialColors
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_utils.HostAppInfo

class ChatNotification {

    companion object {
        const val EXTRA_REPLY = "com.kaleyra.collaboration_suite_core_ui.EXTRA_REPLY"
    }

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
//        var deleteIntent: PendingIntent? = null,
        var fullscreenIntent: PendingIntent? = null,
    ) {
        fun username(text: String) = apply { this.username = text }

        fun userId(text: String) = apply { this.userId = text }

        fun avatar(uri: Uri) = apply { this.avatar = uri }

        fun contentTitle(text: String) = apply { this.contentTitle = text }

        fun messages(list: List<ChatNotificationMessage>) = apply { this.messages = list }

        fun isGroupChat(value: Boolean) = apply { this.isGroupChat = value }

        fun contentIntent(pendingIntent: PendingIntent) =
            apply { this.contentIntent = pendingIntent }

        fun replyIntent(pendingIntent: PendingIntent) =
            apply { this.replyIntent = pendingIntent }

//        fun deleteIntent(pendingIntent: PendingIntent) = apply { this.deleteIntent = pendingIntent }

        fun fullscreenIntent(pendingIntent: PendingIntent) =
            apply { this.fullscreenIntent = pendingIntent }

        fun build(): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(context, channelId, channelName)

            val applicationIcon =
                context.applicationContext.packageManager.getApplicationIcon(HostAppInfo.name)
            val person = Person.Builder()
                .setName(username)
                .setKey(userId)
                .apply { if (avatar != Uri.EMPTY) setIcon(IconCompat.createWithContentUri(avatar)) }
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
                    .setName(it.username)
                    .setKey(it.userId)
                    .apply { if (it.avatar != Uri.EMPTY) setIcon(IconCompat.createWithContentUri(it.avatar)) }
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