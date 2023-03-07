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

package com.kaleyra.collaboration_suite_phone_ui.userdataconsentagreement

import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement.UserDataConsentAgreement

object PhoneUserDataConsentAgreement : UserDataConsentAgreement(
    activityClazz = this::class.java,
    notificationInfo = NotificationInfo("", "", {}),
    activityInfo = ActivityInfo("", "", "", "", {}, {})
) {

    override fun show() = Unit

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
}