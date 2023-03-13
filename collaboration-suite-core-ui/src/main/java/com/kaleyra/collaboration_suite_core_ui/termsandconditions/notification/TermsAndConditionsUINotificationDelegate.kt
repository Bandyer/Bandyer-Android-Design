package com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.TermsAndConditionsUI
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.ContextRetainer

internal class TermsAndConditionsUINotificationDelegate(
    private val notificationConfig: TermsAndConditionsUI.Config.Notification
) : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.terms_and_conditions_notification_channel"
        const val ACTION_CANCEL = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.ACTION_CANCEL"
        const val TERMS_AND_CONDITIONS_NOTIFICATION_ID = 80
        const val FULL_SCREEN_REQUEST_CODE = 1111
        const val CONTENT_REQUEST_CODE = 2222
        const val DELETE_REQUEST_CODE = 3333
    }

    private val context by lazy { ContextRetainer.context }

    private val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    fun showNotification(activityIntent: Intent) {
        context.registerReceiver(this, IntentFilter(ACTION_CANCEL))
        val notification = buildNotification(
            context = context,
            notificationConfig = notificationConfig,
            activityIntent = activityIntent
        )
        notificationManager.notify(TERMS_AND_CONDITIONS_NOTIFICATION_ID, notification)
    }

    fun dismissNotification() {
        NotificationDisposer.revokeDisposal(context, TERMS_AND_CONDITIONS_NOTIFICATION_ID)
        notificationManager.cancel(TERMS_AND_CONDITIONS_NOTIFICATION_ID)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CANCEL) return
        context.unregisterReceiver(this)
        notificationConfig.dismissCallback.invoke()
    }

    private fun buildNotification(
        context: Context,
        notificationConfig: TermsAndConditionsUI.Config.Notification,
        activityIntent: Intent
    ): Notification {
        return TermsAndConditionsNotification.Builder(
            context = context,
            channelId = CHANNEL_ID,
            channelName = context.resources.getString(R.string.kaleyra_notification_terms_and_conditions_channel_name),
            notificationId = TERMS_AND_CONDITIONS_NOTIFICATION_ID
        )
            .title(notificationConfig.title)
            .message(notificationConfig.message)
            .contentIntent(activityPendingIntent(context, activityIntent, CONTENT_REQUEST_CODE))
            .deleteIntent(
                deletePendingIntent(context, Intent(ACTION_CANCEL).apply { `package` = context.applicationContext.packageName })
            )
            .apply {
                if (notificationConfig.enableFullscreen) {
                    fullscreenIntent(activityPendingIntent(context, activityIntent, FULL_SCREEN_REQUEST_CODE))
                }
                if (notificationConfig.timeout != null) {
                    timeout(notificationConfig.timeout)
                }
            }
            .build()
    }

    private fun activityPendingIntent(context: Context, intent: Intent, requestCode: Int) =
        PendingIntent.getActivity(context.applicationContext, requestCode, intent, PendingIntentExtensions.updateFlags)

    private fun deletePendingIntent(context: Context, intent: Intent) =
        PendingIntent.getBroadcast(context.applicationContext, DELETE_REQUEST_CODE, intent, PendingIntentExtensions.updateFlags)

}