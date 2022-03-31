package com.kaleyra.collaboration_suite_core_ui.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isScreenOff

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
        var caller: String? = null,
        var image: Bitmap? = null,
        var contentText: String? = null,
        var contentIntent: PendingIntent? = null,
        var fullscreenIntent: PendingIntent? = null,
        var answerIntent: PendingIntent? = null,
        var declineIntent: PendingIntent? = null,
    ) {
        fun caller(caller: String) = apply { this.caller = caller }

        fun image(image: Bitmap) = apply { this.image = image }

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

            return buildNotificationApi21(
                context,
                type,
                channelId,
                isHighImportance,
                type == Type.ONGOING,
                caller,
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
            usersDescription: String? = null,
            usersAvatar: Bitmap? = null,
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
                // TODO change this with the app icon
                .setSmallIcon(R.drawable.kaleyra_z_audio_only)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .setContentTitle(usersDescription)

            usersAvatar?.also { builder.setLargeIcon(BitmapUtils.roundBitmap(it)) }
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
                R.drawable.kaleyra_z_audio_only,
                context.getString(if (type == Type.INCOMING) R.string.kaleyra_notification_decline else R.string.kaleyra_notification_hangup),
                declineIntent
            )
            builder.addAction(declineAction)

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


//    @RequiresApi(Build.VERSION_CODES.P)
//    private fun buildNotificationApi31(
//        context: Context,
//        caller: String,
//        usersAvatar: Bitmap,
//        channelId: String,
//        contentText: String,
//        contentIntent: PendingIntent? = null,
//        fullscreenIntent: PendingIntent? = null,
//        useTimer: Boolean,
//        actions: List<NotificationCompat.Action> = listOf(),
//        type: NotificationType
//    ): Notification {
//        val person = Person.Builder()
//            .setName(caller)
//            .setIcon(Icon.createWithBitmap(usersAvatar))
//            .build()
//
//        val style1 = when (type) {
//            NotificationType.INCOMING_CALL -> Notification.CallStyle.forIncomingCall(
//                person,
//                callIntent,
//                callIntent
//            )
//            ONGOING_CALL -> Notification.CallStyle.forOngoingCall(
//                person,
//                callIntent
//            )
//        }.apply {
//            setAnswerButtonColorHint(
//                context.resources.getColor(
//                    R.color.kaleyra_color_answer_button,
//                    null
//                )
//            )
//            setDeclineButtonColorHint(
//                context.resources.getColor(
//                    R.color.kaleyra_color_hang_up_button,
//                    null
//                )
//            )
////            setIsVideo(true)
//        }
//
//        val builder = Notification.Builder(context.applicationContext, channelId)
//            .setAutoCancel(false)
//            .setOngoing(true)
//            .setOnlyAlertOnce(true)
//            .setUsesChronometer(useTimer)
//            // TODO change this with the app icon
//            .setSmallIcon(R.drawable.kaleyra_z_audio_only)
//            .setCategory(Notification.CATEGORY_CALL)
//            .setVisibility(Notification.VISIBILITY_PUBLIC)
//            .setContentText(contentText)
//            .addPerson(person)
//            .style = style1
//
//
//        builder.setColorized(true)
//        builder.setColor(
//            context.getThemeAttribute(
//                com.kaleyra.collaboration_suite_phone_ui.R.style.KaleyraCollaborationSuiteUI_Theme_Call,
//                com.kaleyra.collaboration_suite_phone_ui.R.styleable.KaleyraCollaborationSuiteUI_Theme_Call,
//                R.attr.backgroundColor
//            )
//        )
//
//        contentIntent?.also { builder.setContentIntent(it) }
//        fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }
//        actions.forEach { builder.addAction(it) }
//
//        return builder.build()
//    }