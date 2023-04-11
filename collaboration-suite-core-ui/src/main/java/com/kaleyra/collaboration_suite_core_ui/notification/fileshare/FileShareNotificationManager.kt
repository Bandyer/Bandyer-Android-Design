package com.kaleyra.collaboration_suite_core_ui.notification.fileshare

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.sharedfolder.SharedFile
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

interface FileShareNotificationDelegate {

    companion object {
        const val EXTRA_DOWNLOAD_ID = "com.kaleyra.collaboration_suite_core_ui.EXTRA_DOWNLOAD_ID"
    }

    fun syncFileShareNotification(
        call: Call,
        activityClazz: Class<*>,
        scope: CoroutineScope
    ) {
        var lastNotifiedDownload: SharedFile? = null
        val me = call.participants.value.me
        call.sharedFolder.files
            .transform { files ->
                val lastDownload = files.lastOrNull { file -> file.sender.userId != me.userId } ?: return@transform
                if (lastNotifiedDownload != lastDownload) emit(lastDownload)
                lastNotifiedDownload = lastDownload
            }
            .onEach {
                val notification = buildNotification(call, it, activityClazz)
                NotificationManager.notify(it.id.hashCode(), notification)
            }
            .launchIn(scope)
    }

    private suspend fun buildNotification(call: Call, sharedFile: SharedFile, activityClazz: Class<*>): Notification {
        val participants = call.participants.first()
        val participant = participants.others.firstOrNull { it.userId == sharedFile.sender.userId }
        val username = participant?.displayName?.first() ?: ""
        return NotificationManager.buildIncomingFileNotification(username, sharedFile.id, activityClazz)
    }
}

interface FileShareNotificationManager {

    companion object {
        private const val DEFAULT_CHANNEL_ID = "com.kaleyra.collaboration_suite_core_ui.fileshare_notification_channel_default"

        private const val CONTENT_REQUEST_CODE = 121
        private const val DOWNLOAD_REQUEST_CODE = 232
    }

    fun buildIncomingFileNotification(
        username: String,
        downloadId: String,
        activityClazz: Class<*>
    ): Notification {
        val context = ContextRetainer.context
        val resources = context.resources

        val builder = FileShareNotification
            .Builder(
                context = context,
                channelId = DEFAULT_CHANNEL_ID,
                channelName = resources.getString(R.string.kaleyra_notification_file_share_channel_name)
            )
            .contentTitle(resources.getString(
                    R.string.kaleyra_notification_user_sharing_file,
                    username
                ))
            .contentText(resources.getString(R.string.kaleyra_notification_download_file))
            .contentIntent(downloadContentPendingIntent(context, activityClazz, downloadId))
            .downloadIntent(downloadPendingIntent(context, activityClazz, downloadId))

        return builder.build()
    }

    private fun downloadContentPendingIntent(context: Context, activityClazz: Class<*>, downloadId: String) =
        createCallActivityPendingIntent(context, CONTENT_REQUEST_CODE + downloadId.hashCode(), activityClazz)

    private fun downloadPendingIntent(
        context: Context,
        activityClazz: Class<*>,
        downloadId: String
    ) = createCallActivityPendingIntent(
            context,
            DOWNLOAD_REQUEST_CODE + downloadId.hashCode(),
            activityClazz,
            Intent().putExtra(EXTRA_DOWNLOAD_ID, downloadId)
        )

    private fun <T> createCallActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        intentExtras: Intent? = null
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            this.action = Intent.ACTION_MAIN
            this.addCategory(Intent.CATEGORY_LAUNCHER)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.putExtra("action", FileShareNotificationActionReceiver.ACTION_DOWNLOAD)
            intentExtras?.let { this.putExtras(it) }
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }
}

