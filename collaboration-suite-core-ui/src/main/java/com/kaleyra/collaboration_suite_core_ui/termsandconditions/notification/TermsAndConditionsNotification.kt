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

package com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification

import android.app.*
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.video_utils.HostAppInfo

internal class TermsAndConditionsNotification {

    data class Builder(
        val context: Context,
        val channelId: String,
        val channelName: String,
        val notificationId: Int,
        var title: String? = null,
        var message: String? = null,
        var timeout: Long? = null,
        var contentIntent: PendingIntent? = null,
        var fullscreenIntent: PendingIntent? = null,
        var deleteIntent: PendingIntent? = null
    ) {
        fun title(text: String) = apply { this.title = text }

        fun message(text: String) = apply { this.message = text }

        fun timeout(millis: Long) = apply { this.timeout = millis }

        fun contentIntent(pendingIntent: PendingIntent) =
            apply { this.contentIntent = pendingIntent }

        fun fullscreenIntent(pendingIntent: PendingIntent) =
            apply { this.fullscreenIntent = pendingIntent }

        fun deleteIntent(pendingIntent: PendingIntent) =
            apply { this.deleteIntent = pendingIntent }

        fun build(): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(context, channelId, channelName)

            val applicationIcon = context.applicationContext.packageManager.getApplicationIcon(HostAppInfo.name)

            val builder = NotificationCompat.Builder(context.applicationContext, channelId)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_kaleyra_terms_and_conditions)
                .setLargeIcon(applicationIcon.toBitmap())

            timeout?.takeIf { it > 0L }?.also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setTimeoutAfter(it)
                else NotificationDisposer.disposeAfter(context, notificationId, it)
            }

            contentIntent?.also { builder.setContentIntent(it) }
            fullscreenIntent?.also { builder.setFullScreenIntent(it, true) }
            deleteIntent?.also { builder.setDeleteIntent(it) }

            return builder.build()
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