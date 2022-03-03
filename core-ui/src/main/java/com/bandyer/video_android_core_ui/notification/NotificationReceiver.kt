package com.bandyer.video_android_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bandyer.video_android_core_ui.call.CallService

internal class NotificationReceiver: BroadcastReceiver() {

    companion object {
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