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

package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsActivityDelegate
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.TermsAndConditionsNotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditionsConfig
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.TermsAndConditionsNotification
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions

open class TermsAndConditionsManager(
    private val activityClazz: Class<*>,
    private val notificationConfig: TermsAndConditionsConfig.NotificationConfig,
    private val activityConfig: TermsAndConditionsConfig.ActivityConfig
) : TermsAndConditionsNotificationDelegate(), TermsAndConditionsActivityDelegate {

    open fun show() {
        if (AppLifecycle.isInForeground.value) {
            showActivity(activityConfig, activityClazz)
        } else {
            showNotification(notificationConfig, buildActivityIntent(activityConfig, activityClazz))
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
        return TermsAndConditionsNotification.Builder(
            context = context,
            channelId = CHANNEL_ID,
            channelName = context.resources.getString(R.string.kaleyra_notification_terms_and_conditions_channel_name),
            notificationId = TERMS_AND_CONDITIONS_NOTIFICATION_ID
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