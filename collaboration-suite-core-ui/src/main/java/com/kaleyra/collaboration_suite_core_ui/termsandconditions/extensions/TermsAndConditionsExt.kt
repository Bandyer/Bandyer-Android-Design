package com.kaleyra.collaboration_suite_core_ui.termsandconditions.extensions

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.collaboration_suite_utils.ContextRetainer

object TermsAndConditionsExt {
    fun TermsAndConditions.accept() = ContextRetainer.context.apply {
        sendBroadcast(buildBroadcastIntent(TermsAndConditionsUIActivityDelegate.ACTION_ACCEPT))
    }

    fun TermsAndConditions.decline() = ContextRetainer.context.apply {
        sendBroadcast(buildBroadcastIntent(TermsAndConditionsUIActivityDelegate.ACTION_DECLINE))
    }

    private fun Context.buildBroadcastIntent(action: String) = Intent(action).apply { this.`package` = applicationContext.packageName }
}
