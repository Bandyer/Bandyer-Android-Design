package com.kaleyra.video_common_ui.notification.fileshare

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.video_common_ui.utils.PendingIntentExtensions

internal interface FileShareNotificationManager {

    companion object {
        private const val DEFAULT_CHANNEL_ID = "com.kaleyra.video_common_ui.fileshare_notification_channel_default"

        private const val CONTENT_REQUEST_CODE = 121
        private const val DOWNLOAD_REQUEST_CODE = 232
    }

    fun buildIncomingFileNotification(
        context: Context,
        username: String,
        downloadId: String,
        activityClazz: Class<*>
    ): Notification {
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
            // Setting main action and category launcher allows to open activity
            // from notification if there is already an instance, instead of a creating a new one
            this.action = Intent.ACTION_MAIN
            this.addCategory(Intent.CATEGORY_LAUNCHER)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.putExtra("notificationAction", FileShareNotificationActionReceiver.ACTION_DOWNLOAD)
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

