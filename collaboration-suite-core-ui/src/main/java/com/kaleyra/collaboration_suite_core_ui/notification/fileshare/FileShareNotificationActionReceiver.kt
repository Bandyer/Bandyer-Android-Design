package com.kaleyra.collaboration_suite_core_ui.notification.fileshare

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.CollaborationBroadcastReceiver
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.collaboration_suite_core_ui.onCallReady
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileShareNotificationActionReceiver internal constructor(val dispatcher: CoroutineDispatcher = Dispatchers.IO): CollaborationBroadcastReceiver() {

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
        CoroutineScope(dispatcher).launch {
            requestConfigure().let {
                if (!it) return@let context.goToLaunchingActivity()
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