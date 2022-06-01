package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MessagingNotificationReceiver: BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        /**
         * ActionAnswer
         */
        const val ACTION_REPLY = "com.kaleyra.collaboration_suite_core_ui.ACTION_REPLY"

        const val EXTRA_REPLY =
            "com.kaleyra.collaboration_suite_core_ui.EXTRA_REPLY"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

    }
}