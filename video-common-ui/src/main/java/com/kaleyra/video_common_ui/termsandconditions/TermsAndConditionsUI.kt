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

package com.kaleyra.video_common_ui.termsandconditions

import android.content.Context
import com.kaleyra.video_common_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver
import com.kaleyra.video_common_ui.termsandconditions.notification.TermsAndConditionsUINotificationDelegate
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_utils.ContextRetainer

open class TermsAndConditionsUI(
    context: Context = ContextRetainer.context,
    activityClazz: Class<*>,
    private val notificationConfig: Config.Notification,
    private val activityConfig: Config.Activity
) : TermsAndConditionBroadcastReceiver() {

    sealed interface Config {

        val title: String

        val message: String

        data class Notification(
            override val title: String,
            override val message: String,
            val dismissCallback: () -> Unit,
            val enableFullscreen: Boolean = false,
            val timeout: Long? = null
        ) : Config

        data class Activity(
            override val title: String,
            override val message: String,
            val acceptText: String,
            val declineText: String,
            val acceptCallback: () -> Unit,
            val declineCallback: () -> Unit
        ) : Config
    }

    private val activityDelegate = TermsAndConditionsUIActivityDelegate(context, activityConfig, activityClazz)

    private val notificationDelegate = TermsAndConditionsUINotificationDelegate(context, notificationConfig)

    open fun show(context: Context = ContextRetainer.context) {
        registerForTermAndConditionAction(context)
        if (AppLifecycle.isInForeground.value) activityDelegate.showActivity()
        else notificationDelegate.showNotification(activityDelegate.getActivityIntent())
    }

    fun dismiss() = notificationDelegate.dismissNotification()

    override fun onActionAccept() = activityConfig.acceptCallback()

    override fun onActionDecline() = activityConfig.declineCallback()

    override fun onActionCancel() = notificationConfig.dismissCallback()
}