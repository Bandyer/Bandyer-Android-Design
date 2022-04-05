package com.kaleyra.collaboration_suite_core_ui.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_utils.HostAppInfo

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
        var user: String? = null,
        var contentText: String? = null,
        var contentIntent: PendingIntent? = null,
        var fullscreenIntent: PendingIntent? = null,
        var answerIntent: PendingIntent? = null,
        var declineIntent: PendingIntent? = null,
    ) {
        fun user(user: String) = apply { this.user = user }

        fun importance(isHigh: Boolean) = apply { this.isHighImportance = isHigh }

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
                context = context,
                type = type,
                channelId = channelId,
                isHighPriority = isHighImportance,
                useTimer = type == Type.ONGOING,
                user = user,
                contentText = contentText,
                contentIntent = contentIntent,
                fullscreenIntent = fullscreenIntent,
                answerIntent = answerIntent,
                declineIntent = declineIntent
            ) else buildNotificationApi31(
                context = context,
                type = type,
                channelId = channelId,
                useTimer = type == Type.ONGOING,
                user = user,
                contentText = contentText,
                contentIntent = contentIntent,
                fullscreenIntent = fullscreenIntent,
                answerIntent = answerIntent,
                declineIntent = declineIntent
            )
        }

        private fun buildNotificationApi21(
            context: Context,
            type: Type,
            channelId: String,
            isHighPriority: Boolean,
            useTimer: Boolean,
            user: String? = null,
            contentText: String? = null,
            contentIntent: PendingIntent? = null,
            fullscreenIntent: PendingIntent? = null,
            answerIntent: PendingIntent? = null,
            declineIntent: PendingIntent? = null
        ): Notification {
            val applicationIcon = context.applicationContext.packageManager.getApplicationIcon(HostAppInfo.name)
            val builder = NotificationCompat.Builder(context.applicationContext, channelId)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(useTimer)
                .setSmallIcon(R.drawable.kaleyra_z_audio_only)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .setContentTitle(user)
                .setLargeIcon(applicationIcon.toBitmap())

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

        // Be aware callStyle notifications require either to be linked to a foreground service or have a fullscreen intent
        @RequiresApi(Build.VERSION_CODES.S)
        private fun buildNotificationApi31(
            context: Context,
            type: Type,
            channelId: String,
            useTimer: Boolean,
            user: String? = null,
            contentText: String? = null,
            contentIntent: PendingIntent? = null,
            fullscreenIntent: PendingIntent? = null,
            answerIntent: PendingIntent? = null,
            declineIntent: PendingIntent? = null
        ): Notification {
            val applicationIcon = context.applicationContext.packageManager.getApplicationIcon(HostAppInfo.name)
            val person = Person.Builder()
                .setName(user)
                .setIcon(Icon.createWithBitmap(applicationIcon.toBitmap()))
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
            }

            val builder = Notification.Builder(context.applicationContext, channelId)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(useTimer)
                .setSmallIcon(R.drawable.kaleyra_z_audio_only)
                .setCategory(Notification.CATEGORY_CALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .addPerson(person)
                .setStyle(style)

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