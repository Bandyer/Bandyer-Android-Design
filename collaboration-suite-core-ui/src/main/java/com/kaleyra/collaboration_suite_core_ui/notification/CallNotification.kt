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
class CallNotification {

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
     * Builder
     *
     * @property context The context used to construct the notification
     * @property channelId The notification channel id
     * @property channelName The notification channel name showed to the users
     * @property type The notification type
     * @property isHighImportance True to set the notification with high importance/priority
     * @property color The color used as notification accent color
     * @property smallIconResource The resource to be used as small icon of the notification
     * @property user The user to be show in the notification
     * @property contentText The text to be shown inside the notification
     * @property enableTimer Enable the timer of the notification
     * @property contentIntent The pending intent to be executed when the user tap on the notification
     * @property fullscreenIntent The pending intent to be executed when notification is in the lock screen
     * @property answerIntent The pending intent to be executed when the user taps the answer button
     * @property declineIntent The pending intent to be executed when the user taps the decline button
     * @property screenShareIntent The pending intent to be executed when the user taps the stop screen share button
     * @constructor
     */
    data class Builder(
        val context: Context,
        val channelId: String,
        val channelName: String,
        val type: Type,
        var isHighImportance: Boolean = false,
        var enableCallStyle: Boolean = true,
        var color: Int? = null,
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
         * Set the notification call style
         *
         * @param value True if to enable call style notification, false otherwise
         * @return Builder
         */
        fun enableCallStyle(value: Boolean) = apply { this.enableCallStyle = value }

        /**
         * Set the color used as notification accent color
         *
         * @param color notification accent color
         * @return Builder
         */
        fun color(color: Int) = apply { this.color = color }

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
                color = color,
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
                color = color,
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
            color: Int?,
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
                .setSound(null)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(enableTimer)
                .setSmallIcon(R.drawable.ic_kaleyra_answer)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(contentText)
                .setContentTitle(user)
                .setLargeIcon(applicationIcon.toBitmap())

            color?.let { builder.setColor(it) }
            contentIntent?.also { builder.setContentIntent(it) }
            fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }

            var screenShareAction: NotificationCompat.Action? = null
            screenShareIntent?.also {
                screenShareAction = NotificationCompat.Action(
                    R.drawable.ic_kaleyra_screen_share,
                    context.getString(R.string.kaleyra_notification_stop_screen_share),
                    it)
            }

            var answerAction: NotificationCompat.Action? = null
            if (type == Type.INCOMING) {
                answerAction = NotificationCompat.Action(
                    R.drawable.ic_kaleyra_answer,
                    context.getString(R.string.kaleyra_notification_answer),
                    answerIntent
                )
            }

            val declineAction = NotificationCompat.Action(
                R.drawable.ic_kaleyra_decline,
                context.getString(if (type == Type.INCOMING) R.string.kaleyra_notification_decline else R.string.kaleyra_notification_hangup),
                declineIntent)

            screenShareAction?.let { builder.addAction(it) }
            builder.addAction(declineAction)
            answerAction?.let { builder.addAction(it) }

            return builder.build()
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun buildNotificationApi31(
            context: Context,
            type: Type,
            channelId: String,
            enableTimer: Boolean,
            color: Int?,
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
                .setName(user?.takeIf { it.isNotEmpty() } ?: " ")
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

            if (enableCallStyle) {
                builder.style = style
            } else {
                when(type) {
                    Type.INCOMING -> {
                        val answerAction = Notification.Action.Builder(
                            Icon.createWithResource(context, R.drawable.ic_kaleyra_answer),
                            context.getString(R.string.kaleyra_notification_answer),
                            answerIntent
                        ).build()
                        val declineAction = Notification.Action.Builder(
                            Icon.createWithResource(context, R.drawable.ic_kaleyra_decline),
                            context.getString(R.string.kaleyra_notification_decline),
                            declineIntent
                        ).build()
                        builder.setActions(declineAction, answerAction)
                    }
                    else -> {
                        val declineAction = Notification.Action.Builder(
                            Icon.createWithResource(context, R.drawable.ic_kaleyra_decline),
                            context.getString(R.string.kaleyra_notification_hangup),
                            declineIntent
                        ).build()
                        builder.setActions(declineAction)
                    }
                }
            }

            color?.let { builder.setColor(it) }
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
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}