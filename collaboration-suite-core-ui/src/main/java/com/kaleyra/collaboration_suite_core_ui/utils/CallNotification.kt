package com.kaleyra.collaboration_suite_core_ui.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.getApplicationIconId


internal class CallNotification {

    enum class Type {
        INCOMING,
        ONGOING,
        OUTGOING
    }

    data class Builder(
        val context: Context,
        val channelId: String,
        val channelName: String,
        val type: Type,
        var isHighImportance: Boolean = false,
        var isVideo: Boolean = false,
        var user: String? = null,
        var image: Bitmap? = null,
        var contentText: String? = null,
        var contentIntent: PendingIntent? = null,
        var fullscreenIntent: PendingIntent? = null,
        var answerIntent: PendingIntent? = null,
        var declineIntent: PendingIntent? = null,
    ) {
        fun user(user: String) = apply { this.user = user }

        fun image(image: Bitmap) = apply { this.image = image }

        fun importance(isHigh: Boolean) = apply { this.isHighImportance = isHigh }

        fun isVideo(isVideo: Boolean) = apply { this.isVideo = isVideo }

        fun contentText(text: String) = apply {
            this.contentText = text
        }

        fun contentIntent(pendingIntent: PendingIntent) = apply {
            this.contentIntent = pendingIntent
        }

        fun fullscreenIntent(pendingIntent: PendingIntent) = apply {
            this.fullscreenIntent = pendingIntent
        }

        fun answerIntent(pendingIntent: PendingIntent) = apply {
            this.answerIntent = pendingIntent
        }

        fun declineIntent(pendingIntent: PendingIntent) = apply {
            this.declineIntent = pendingIntent
        }

        fun build(): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(context, channelId, channelName, isHighImportance)

            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) buildNotificationApi21(
                context,
                type,
                channelId,
                isHighImportance,
                type == Type.ONGOING,
                user,
                image,
                contentText,
                contentIntent,
                fullscreenIntent,
                answerIntent,
                declineIntent
            ) else buildNotificationApi31(
                context,
                type,
                channelId,
                type == Type.ONGOING,
                isVideo,
                user,
                image,
                contentText,
                contentIntent,
                fullscreenIntent,
                answerIntent,
                declineIntent
            )
        }

        private fun buildNotificationApi21(
            context: Context,
            type: Type,
            channelId: String,
            isHighPriority: Boolean,
            useTimer: Boolean,
            user: String? = null,
            icon: Bitmap? = null,
            contentText: String? = null,
            contentIntent: PendingIntent? = null,
            fullscreenIntent: PendingIntent? = null,
            answerIntent: PendingIntent? = null,
            declineIntent: PendingIntent? = null
        ): Notification {
            val builder = NotificationCompat.Builder(context.applicationContext, channelId)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(useTimer)
                .setSmallIcon(context.getApplicationIconId())
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .setContentTitle(user)

            icon?.also { builder.setLargeIcon(BitmapUtils.roundBitmap(icon)) }
            contentIntent?.also { builder.setContentIntent(it) }
            fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }

            if (type == Type.INCOMING) {
                val answerAction = NotificationCompat.Action(
                    R.drawable.kaleyra_z_audio_only,
                    context.getString(R.string.kaleyra_notification_answer),
                    answerIntent
                )
                builder.addAction(answerAction)
            }

            val declineAction = NotificationCompat.Action(
                R.drawable.kaleyra_z_end_call,
                context.getString(if (type == Type.INCOMING) R.string.kaleyra_notification_decline else R.string.kaleyra_notification_hangup),
                declineIntent
            )
            builder.addAction(declineAction)

            return builder.build()
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun buildNotificationApi31(
            context: Context,
            type: Type,
            channelId: String,
            isVideo: Boolean,
            useTimer: Boolean,
            user: String? = null,
            icon: Bitmap? = null,
            contentText: String? = null,
            contentIntent: PendingIntent? = null,
            fullscreenIntent: PendingIntent? = null,
            answerIntent: PendingIntent? = null,
            declineIntent: PendingIntent? = null
        ): Notification {
            val person = Person.Builder()
                .setName(user)
                .setIcon(Icon.createWithBitmap(BitmapUtils.roundBitmap(icon!!)))
                .build()

            val defaultIntent =
                PendingIntent.getActivity(context, 0, Intent(), PendingIntentExtensions.updateFlags)

            val style = when (type) {
                Type.INCOMING -> Notification.CallStyle.forIncomingCall(
                    person,
                    declineIntent ?: defaultIntent,
                    answerIntent ?: defaultIntent
                )
                else -> Notification.CallStyle.forOngoingCall(
                    person,
                    declineIntent ?: defaultIntent
                )
            }.apply {
                setAnswerButtonColorHint(
                    context.resources.getColor(
                        R.color.kaleyra_color_answer_button,
                        null
                    )
                )
                setDeclineButtonColorHint(
                    context.resources.getColor(
                        R.color.kaleyra_color_hang_up_button,
                        null
                    )
                )
            }

            val builder = Notification.Builder(context.applicationContext, channelId)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(useTimer)
                .setSmallIcon(if (isVideo) R.drawable.kaleyra_z_cam_22 else R.drawable.kaleyra_z_audio_only)
                .setCategory(Notification.CATEGORY_CALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .addPerson(person)
                .setStyle(style)
                .setColorized(true)
                .setColor(Color.WHITE)

            contentIntent?.also { builder.setContentIntent(it) }
            fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }

            return builder.build()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(
            context: Context,
            channelId: String,
            channelName: String,
            isHighImportance: Boolean
        ) {
            val notificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                if (isHighImportance) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}