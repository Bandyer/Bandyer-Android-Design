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

package com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions

internal class AutoDismissNotification : BroadcastReceiver() {

    companion object {
        private const val KEY_EXTRA_NOTIFICATION_ID = "notification_id"

        fun setAlarm(context: Context, notificationId: Int, time: Long) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AutoDismissNotification::class.java)
            alarmIntent.putExtra(KEY_EXTRA_NOTIFICATION_ID, notificationId)
            val alarmPendingIntent = PendingIntent.getBroadcast(context, notificationId, alarmIntent, PendingIntentExtensions.oneShotFlags)
            alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, alarmPendingIntent)
        }

        fun cancelAlarm(context: Context, notificationId: Int) {
            val alarmIntent = Intent(context, AutoDismissNotification::class.java)
            alarmIntent.putExtra(KEY_EXTRA_NOTIFICATION_ID, notificationId)
            val alarmPendingIntent = PendingIntent.getBroadcast(context, notificationId, alarmIntent, PendingIntentExtensions.oneShotFlags)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(alarmPendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(intent.getIntExtra(KEY_EXTRA_NOTIFICATION_ID, 0))
    }
}