package com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions

class AutoDismissNotification : BroadcastReceiver() {

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