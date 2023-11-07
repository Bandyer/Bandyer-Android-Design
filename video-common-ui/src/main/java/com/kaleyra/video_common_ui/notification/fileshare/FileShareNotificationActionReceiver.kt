package com.kaleyra.video_common_ui.notification.fileshare

import android.content.Context
import android.content.Intent
import com.kaleyra.video_common_ui.KaleyraVideoBroadcastReceiver
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileShareNotificationActionReceiver internal constructor(val dispatcher: CoroutineDispatcher = Dispatchers.IO): KaleyraVideoBroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        /**
         * ActionHangUp
         */
        const val ACTION_DOWNLOAD = "com.kaleyra.video_common_ui.DOWNLOAD"
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(dispatcher).launch {
            requestConfigure().let {
                if (!it) return@let context.goToLaunchingActivity()
                KaleyraVideo.onCallReady(this) { call ->
                    when (intent.extras?.getString("notificationAction")) {
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