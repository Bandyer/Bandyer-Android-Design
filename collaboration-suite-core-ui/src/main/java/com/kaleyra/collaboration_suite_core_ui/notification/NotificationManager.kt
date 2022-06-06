/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * NotificationManager
 */
internal object NotificationManager: CallNotificationManager, ChatNotificationManager {
    /**
     * Notify the system to add a notification
     *
     * @param notificationId Int
     * @param notification Notification
     */
    fun notify(notificationId: Int, notification: Notification) {
        NotificationManagerCompat.from(ContextRetainer.context).notify(
            notificationId, notification
        )
    }

    /**
     * Cancel a notification
     *
     * @param notificationId Int
     */
    fun cancelNotification(notificationId: Int) {
        val notificationManager =
            ContextRetainer.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
