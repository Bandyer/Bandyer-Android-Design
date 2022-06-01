package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChatNotificationActionReceiver: BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        const val ACTION_REPLY = "com.kaleyra.collaboration_suite_core_ui.ACTION_REPLY"

        const val ACTION_DELETE = "com.kaleyra.collaboration_suite_core_ui.ACTION_DELETE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

    }
}