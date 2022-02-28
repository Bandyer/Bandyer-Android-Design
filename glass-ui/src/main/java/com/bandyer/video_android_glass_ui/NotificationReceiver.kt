package com.bandyer.video_android_glass_ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver: BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = "com.bandyer.video_android_glass_ui.NOTIFICATION_ID"
        const val ACTION_ANSWER = "com.bandyer.video_android_glass_ui.ANSWER"
        const val ACTION_HANGUP = "com.bandyer.video_android_glass_ui.HANGUP"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return

        when (intent.action) {
            ACTION_ANSWER -> CallService.onNotificationAnswer()
            ACTION_HANGUP -> CallService.onNotificationHangUp()
            else -> Unit
        }
    }
}