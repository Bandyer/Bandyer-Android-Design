package com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.ContextRetainer

object UserDataConsentAgreement {

    private const val CHANNEL_ID = "com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement.userdataconsentagreement_notification_channel"
    private const val FULL_SCREEN_REQUEST_CODE = 1111
    private const val CONTENT_REQUEST_CODE = 2222
    private const val DELETE_REQUEST_CODE = 3333
    private const val USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID = 80

    private val context by lazy { ContextRetainer.context }

    private val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    fun showNotification(
        title: String,
        message: String,
        contentIntent: Intent,
        deleteIntent: Intent,
        fullscreenIntent: Intent? = null,
        timeoutMs: Long? = null
    ) {
        val notification = buildNotification(
            context = context,
            title = title,
            message = message,
            contentIntent = contentIntent,
            deleteIntent = deleteIntent,
            fullscreenIntent = fullscreenIntent,
            timeoutMs = timeoutMs
        )
        notificationManager.notify(USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID, notification)
    }

    fun show() {
//        if (AppLifecycle.isInForeground.value)
//            else {
//            val notification = buildNotification(context, activityIntent, deleteIntent, title, message, timeoutMs)
//            notificationManager.notify(USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID, notification)
//        }
    }

    fun cancel() {
        AutoDismissNotification.cancelAlarm(context, USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID)
        notificationManager.cancel(USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID)
    }

    private fun buildNotification(
        context: Context,
        title: String,
        message: String,
        contentIntent: Intent,
        deleteIntent: Intent,
        fullscreenIntent: Intent?,
        timeoutMs: Long?
    ): Notification {
        return UserDataConsentAgreementNotification.Builder(
            context = context,
            channelId = CHANNEL_ID,
            channelName = context.resources.getString(R.string.kaleyra_notification_user_data_consent_agreement_channel_name),
            notificationId = USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID
        )
            .title(title)
            .message(message)
            .contentIntent(createActivityPendingIntent(context, CONTENT_REQUEST_CODE, contentIntent))
            .deleteIntent(createDeletePendingIntent(context, deleteIntent))
            .apply {
                if (fullscreenIntent != null) fullscreenIntent(createActivityPendingIntent(context, FULL_SCREEN_REQUEST_CODE, fullscreenIntent))
                if (timeoutMs != null) timeout(timeoutMs)
            }
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

    private fun createDeletePendingIntent(context: Context, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context.applicationContext,
            DELETE_REQUEST_CODE,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }

}