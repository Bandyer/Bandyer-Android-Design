/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *           
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_utils.ContextRetainer

sealed interface UserDataConsentAgreementInfo {

    val title: String

    val message: String

    data class Notification(
        override val title: String,
        override val message: String,
        val dismissCallback: () -> Unit,
        val enableFullscreen: Boolean = false,
        val timeout: Long? = null
    ) : UserDataConsentAgreementInfo

    data class Activity(
        override val title: String,
        override val message: String,
        val acceptText: String,
        val declineText: String,
        val acceptCallback: () -> Unit,
        val declineCallback: () -> Unit
    ) : UserDataConsentAgreementInfo
}

abstract class UserDataConsentAgreementNotificationDelegate : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement.userdataconsentagreement_notification_channel"
        const val ACTION_CANCEL = "com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement.ACTION_CANCEL"
        const val USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID = 80
        const val FULL_SCREEN_REQUEST_CODE = 1111
        const val CONTENT_REQUEST_CODE = 2222
        const val DELETE_REQUEST_CODE = 3333
    }

    private val context by lazy { ContextRetainer.context }

    private val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private var onDismissCallback: (() -> Unit)? = null

    fun showNotification(
        notificationInfo: UserDataConsentAgreementInfo.Notification,
        activityIntent: Intent
    ) {
        context.registerReceiver(this, IntentFilter(ACTION_CANCEL))
        onDismissCallback = notificationInfo.dismissCallback
        val notification = buildNotification(
            context = context,
            notificationInfo = notificationInfo,
            activityIntent = activityIntent
        )
        notificationManager.notify(USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID, notification)
    }

    fun dismissNotification() {
        AutoDismissNotification.cancelAlarm(context, USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID)
        notificationManager.cancel(USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CANCEL) return
        context.unregisterReceiver(this)
        onDismissCallback?.invoke()
        onDismissCallback = null
    }

    private fun buildNotification(
        context: Context,
        notificationInfo: UserDataConsentAgreementInfo.Notification,
        activityIntent: Intent
    ): Notification {
        return UserDataConsentAgreementNotification.Builder(
            context = context,
            channelId = CHANNEL_ID,
            channelName = context.resources.getString(R.string.kaleyra_notification_user_data_consent_agreement_channel_name),
            notificationId = USER_DATA_CONSENT_AGREEMENT_NOTIFICATION_ID
        )
            .title(notificationInfo.title)
            .message(notificationInfo.message)
            .contentIntent(activityPendingIntent(context, activityIntent, CONTENT_REQUEST_CODE))
            .deleteIntent(
                deletePendingIntent(context, Intent(ACTION_CANCEL).apply { `package` = context.applicationContext.packageName })
            )
            .apply {
                if (notificationInfo.enableFullscreen) {
                    fullscreenIntent(activityPendingIntent(context, activityIntent, FULL_SCREEN_REQUEST_CODE))
                }
                if (notificationInfo.timeout != null) {
                    timeout(notificationInfo.timeout)
                }
            }
            .build()
    }

    private fun activityPendingIntent(context: Context, intent: Intent, requestCode: Int) =
        PendingIntent.getActivity(context.applicationContext, requestCode, intent, PendingIntentExtensions.updateFlags)

    private fun deletePendingIntent(context: Context, intent: Intent) =
        PendingIntent.getBroadcast(context.applicationContext, DELETE_REQUEST_CODE, intent, PendingIntentExtensions.updateFlags)

}

interface UserDataConsentAgreementActivityDelegate {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_ACCEPT_TEXT = "acceptText"
        const val EXTRA_DECLINE_TEXT = "declineText"
        const val EXTRA_ACCEPT_CALLBACK = "acceptCallBack"
        const val EXTRA_DECLINE_CALLBACK = "declineCallback"
    }

    fun showActivity(activityInfo: UserDataConsentAgreementInfo.Activity, activityClazz: Class<*>) {
        ContextRetainer.context.startActivity(buildActivityIntent(activityInfo, activityClazz))
    }

    fun dismissActivity() {

    }

    fun buildActivityIntent(activityInfo: UserDataConsentAgreementInfo.Activity, activityClazz: Class<*>) = Intent(ContextRetainer.context, activityClazz).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra("enableTilt", DeviceUtils.isSmartGlass)
        putExtra(EXTRA_TITLE, activityInfo.title)
        putExtra(EXTRA_MESSAGE, activityInfo.message)
        putExtra(EXTRA_ACCEPT_TEXT, activityInfo.acceptText)
        putExtra(EXTRA_DECLINE_TEXT, activityInfo.declineText)
        putExtra(EXTRA_ACCEPT_CALLBACK, ParcelableLambda(activityInfo.acceptCallback))
        putExtra(EXTRA_DECLINE_CALLBACK, ParcelableLambda(activityInfo.declineCallback))
    }
}

open class UserDataConsentAgreement(
    private val activityClazz: Class<*>,
    private val notificationInfo: UserDataConsentAgreementInfo.Notification,
    private val activityInfo: UserDataConsentAgreementInfo.Activity
) : UserDataConsentAgreementNotificationDelegate(), UserDataConsentAgreementActivityDelegate {

    open fun show() {
        if (AppLifecycle.isInForeground.value) {
            showActivity(activityInfo, activityClazz)
        } else {
            showNotification(notificationInfo, buildActivityIntent(activityInfo, activityClazz))
        }
    }

    fun dismiss() {
        dismissNotification()
        dismissActivity()
    }

    // TODO remove the following functions when sdk will be deprecated in favor of collaboration

    protected fun buildNotification(
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