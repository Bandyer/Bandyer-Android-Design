package com.kaleyra.collaboration_suite_core_ui.vppa

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class VPPANotice(
    private val id: String,
    private val title: String,
    private val message: String,
    private val notificationTitle: String,
    private val notificationMessage: String,
    private val acceptText: String,
    private val declineText: String
) {
    private companion object {
        const val CHANNEL_ID = "com.kaleyra.collaboration_suite_core_ui.vppa.vppa_notification_channel"
        const val FULL_SCREEN_REQUEST_CODE = 1111
        const val CONTENT_REQUEST_CODE = 2222
        const val DELETE_REQUEST_CODE = 3333
        const val VPPA_NOTIFICATION_ID = 80
    }

    private val context by lazy { ContextRetainer.context }

    private val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    fun show() {
        val intent = Intent(context.applicationContext, VPPAActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(VPPAActivity.EXTRA_TITLE, title)
            putExtra(VPPAActivity.EXTRA_MESSAGE, message)
            putExtra(VPPAActivity.EXTRA_ACCEPT_TEXT, acceptText)
            putExtra(VPPAActivity.EXTRA_DECLINE_TEXT, declineText)
            putExtra(VPPAActivity.EXTRA_ID, id)
        }
        if (AppLifecycle.isInForeground.value) VPPAActivity.show(context, intent)
        else notificationManager.notify(VPPA_NOTIFICATION_ID, buildNotification(context, intent, id))
    }

    fun cancel() {
        notificationManager.cancel(VPPA_NOTIFICATION_ID)
        VPPAActivity.close()
    }

    private fun buildNotification(context: Context, intent: Intent, id: String): Notification {
        return VPPANotification.Builder(
            context = context,
            channelId = CHANNEL_ID,
            channelName = context.resources.getString(R.string.kaleyra_notification_terms_condition_channel_name)
        )
            .title(notificationTitle)
            .message(notificationMessage)
            .contentIntent(createActivityPendingIntent(context, CONTENT_REQUEST_CODE, intent))
            .fullscreenIntent(createActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE, intent))
            .deleteIntent(createDeletePendingIntent(context, id))
            .build()
    }

    private fun createActivityPendingIntent(
        context: Context,
        requestCode: Int,
        intent: Intent
    ): PendingIntent {
        return PendingIntent.getActivity(
            context.applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

    private fun createDeletePendingIntent(
        context: Context,
        id: String
    ): PendingIntent {
        val packageName = context.applicationContext.packageName
        val intent = Intent(context, VPPABroadcastReceiver::class.java).apply {
            this.`package` = packageName
            this.action = VPPABroadcastReceiver.ACTION_CANCEL
            putExtra(VPPABroadcastReceiver.EXTRA_ID, id)
        }
        return PendingIntent.getBroadcast(
            context.applicationContext,
            DELETE_REQUEST_CODE,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

}