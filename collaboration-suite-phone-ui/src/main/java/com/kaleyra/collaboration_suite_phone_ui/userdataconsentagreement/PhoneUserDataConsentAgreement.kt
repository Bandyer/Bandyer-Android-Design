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