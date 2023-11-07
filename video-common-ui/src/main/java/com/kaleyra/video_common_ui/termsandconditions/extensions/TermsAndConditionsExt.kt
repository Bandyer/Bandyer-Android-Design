package com.kaleyra.video_common_ui.termsandconditions.extensions

import android.content.Context
import android.content.Intent
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_ACCEPT
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_DECLINE
import com.kaleyra.video_common_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.video_utils.ContextRetainer

object TermsAndConditionsExt {
    fun TermsAndConditions.accept() = ContextRetainer.context.apply {
        sendBroadcast(buildBroadcastIntent(ACTION_ACCEPT))
    }

    fun TermsAndConditions.decline() = ContextRetainer.context.apply {
        sendBroadcast(buildBroadcastIntent(ACTION_DECLINE))
    }

    private fun Context.buildBroadcastIntent(action: String) = Intent(action).apply { this.`package` = applicationContext.packageName }
}
