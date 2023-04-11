package com.kaleyra.collaboration_suite_core_ui.notification.fileshare

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.CollaborationBroadcastReceiver
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.collaboration_suite_core_ui.onCallReady
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileShareNotificationActionReceiver : CollaborationBroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        /**
         * ActionHangUp
         */
        const val ACTION_DOWNLOAD = "com.kaleyra.collaboration_suite_core_ui.DOWNLOAD"
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            requestConfigure().let {
                if (!it) return@let ContextRetainer.context.goToLaunchingActivity()
                CollaborationUI.onCallReady(this) { call ->
                    when (intent.action) {
                        ACTION_DOWNLOAD -> {
                            val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID) ?: return@onCallReady
                            call.sharedFolder.download(downloadId)
                            NotificationManager.cancel(downloadId.hashCode())
                        }
                        else -> Unit
                    }
                }
            }
            pendingResult.finish()
        }
    }
}