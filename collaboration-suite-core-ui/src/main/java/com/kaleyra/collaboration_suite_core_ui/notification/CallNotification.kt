package com.kaleyra.collaboration_suite_core_ui.notification

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
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.HostAppInfo

/**
 * CallNotification. Be aware: on api > 31, the notifications requires either to be linked to a foreground service or to have a fullscreen intent.
 */
internal class CallNotification {

    /**
     * Different type of call notifications
     */
    enum class Type {
        /**
         * i n c o m i n g
         */
        INCOMING,

        /**
         * o n g o i n g
         */
        ONGOING,

        /**
         * o u t g o i n g
         */
        OUTGOING
    }

    /**
     * Constructor
     *
     * @property context The context used to construct the notification. Mandatory.
     * @property channelId The notification channel id. Mandatory.
     * @property channelName The notification channel name showed to the users. Mandatory.
     * @property type The notification type. Mandatory.
     * @property isHighImportance True to set the notification with high importance/priority. Optional.
     * @property user The user to be show in the notification. Optional.
     * @property contentText The text to be shown inside the notification. Optional.
     * @property contentIntent The pending intent to be executed when the user tap on the notification. Optional.
     * @property fullscreenIntent The pending intent to be executed when notification is in the lock screen. Optional.
     * @property answerIntent The pending intent to be executed when the user taps the answer button. Optional.
     * @property declineIntent The pending intent to be executed when the user taps the decline button. Optional.
     * @property screenShareIntent The pending intent to be executed when the user taps the stop screen share button
     * @constructor
     */
    data class Builder(
        val context: Context,
        val channelId: String,
        val channelName: String,
        val type: Type,
        var isHighImportance: Boolean = false,
        var user: String? = null,
        var enableTimer: Boolean = false,
        var contentText: String? = null,
        var contentIntent: PendingIntent? = null,
        var fullscreenIntent: PendingIntent? = null,
        var answerIntent: PendingIntent? = null,
        var declineIntent: PendingIntent? = null,
        var screenShareIntent: PendingIntent? = null
    ) {
        /**
         * Set the user
         *
         * @param user String
         * @return Builder
         */
        fun user(user: String) = apply { this.user = user }

        /**
         * Set the notification importance/priority
         *
         * @param isHigh True if the notification should have an high importance, false otherwise
         * @return Builder
         */
        fun importance(isHigh: Boolean) = apply { this.isHighImportance = isHigh }

        /**
         * Enable the notification timer
         *
         * @param enable String
         * @return Builder
         */
        fun timer(enable: Boolean) = apply { this.enableTimer = enable }

        /**
         * The text to be shown inside the notification
         *
         * @param text String
         * @return Builder
         */
        fun contentText(text: String) = apply { this.contentText = text }

        /**
         * The pending intent to be executed when the user tap on the notification
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun contentIntent(pendingIntent: PendingIntent) =
            apply { this.contentIntent = pendingIntent }

        /**
         * The pending intent to be executed when notification is in the lock screen
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun fullscreenIntent(pendingIntent: PendingIntent) =
            apply { this.fullscreenIntent = pendingIntent }

        /**
         * The pending intent to be executed when the user taps the answer button
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun answerIntent(pendingIntent: PendingIntent) = apply { this.answerIntent = pendingIntent }

        /**
         * The pending intent to be executed when the user taps the decline button
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun declineIntent(pendingIntent: PendingIntent) =
            apply { this.declineIntent = pendingIntent }

        /**
         * The pending intent to be executed when the user taps the stop screen share button
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun screenShareIntent(pendingIntent: PendingIntent) =
            apply { this.screenShareIntent = pendingIntent }

        /**
         * Build the call notification
         *
         * @return Notification
         */
        fun build(): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(context, channelId, channelName, isHighImportance)

            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) buildNotificationApi21(
                context = context,
                type = type,
                channelId = channelId,
                isHighPriority = isHighImportance,
                enableTimer = enableTimer,
                user = user,
                contentText = contentText,
                screenShareIntent = screenShareIntent,
                contentIntent = contentIntent,
                fullscreenIntent = fullscreenIntent,
                answerIntent = answerIntent,
                declineIntent = declineIntent
            ) else buildNotificationApi31(
                context = context,
                type = type,
                channelId = channelId,
                enableTimer = enableTimer,
                user = user,
                contentText = contentText,
                screenShareIntent = screenShareIntent,
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
            enableTimer: Boolean,
            user: String? = null,
            contentText: String? = null,
            contentIntent: PendingIntent? = null,
            fullscreenIntent: PendingIntent? = null,
            answerIntent: PendingIntent? = null,
            declineIntent: PendingIntent? = null,
            screenShareIntent: PendingIntent? = null
        ): Notification {
            val applicationIcon =
                context.applicationContext.packageManager.getApplicationIcon(HostAppInfo.name)
            val builder = NotificationCompat.Builder(context.applicationContext, channelId)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(enableTimer)
                .setSmallIcon(R.drawable.ic_kaleyra_answer)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .setContentTitle(user)
                .setLargeIcon(applicationIcon.toBitmap())

            contentIntent?.also { builder.setContentIntent(it) }
            fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }
            screenShareIntent?.also {
                val screenShareAction = NotificationCompat.Action(
                    R.drawable.ic_kaleyra_screen_share,
                    context.getString(R.string.kaleyra_notification_stop_screen_share),
                    it
                )
                builder.addAction(screenShareAction)
            }

            if (type == Type.INCOMING) {
                val answerAction = NotificationCompat.Action(
                    R.drawable.ic_kaleyra_answer,
                    context.getString(R.string.kaleyra_notification_answer),
                    answerIntent
                )
                builder.addAction(answerAction)
            }

            val declineAction = NotificationCompat.Action(
                R.drawable.ic_kaleyra_decline,
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
            enableTimer: Boolean,
            user: String? = null,
            contentText: String? = null,
            contentIntent: PendingIntent? = null,
            fullscreenIntent: PendingIntent? = null,
            answerIntent: PendingIntent? = null,
            declineIntent: PendingIntent? = null,
            screenShareIntent: PendingIntent? = null
        ): Notification {
            val applicationIcon =
                context.applicationContext.packageManager.getApplicationIcon(HostAppInfo.name)
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
                .setUsesChronometer(enableTimer)
                .setSmallIcon(R.drawable.ic_kaleyra_answer)
                .setCategory(Notification.CATEGORY_CALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .addPerson(person)
                .setStyle(style)

            contentIntent?.also { builder.setContentIntent(it) }
            fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }
            screenShareIntent?.also {
                val screenShareAction = Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_kaleyra_screen_share),
                    context.getString(R.string.kaleyra_notification_stop_screen_share),
                    it
                ).build()
                builder.addAction(screenShareAction)
            }

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