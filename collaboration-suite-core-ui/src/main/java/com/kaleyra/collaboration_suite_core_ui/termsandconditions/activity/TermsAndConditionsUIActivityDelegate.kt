package com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.TermsAndConditionsUI
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.constants.Constants.EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_utils.ContextRetainer

internal class TermsAndConditionsUIActivityDelegate(
    private val activityConfig: TermsAndConditionsUI.Config.Activity,
    private val activityClazz: Class<*>
) : BroadcastReceiver() {

    companion object {
        const val ACTION_ACCEPT = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.ACTION_ACCEPT"
        const val ACTION_DECLINE = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.ACTION_DECLINE"
    }

    private val context by lazy { ContextRetainer.context }

    fun showActivity() {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(ACTION_ACCEPT)
            addAction(ACTION_DECLINE)
        })
        context.startActivity(buildActivityIntent(activityConfig, activityClazz))
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ACCEPT && intent.action != ACTION_DECLINE) return
        when (intent.action) {
            ACTION_ACCEPT -> activityConfig.acceptCallback()
            ACTION_DECLINE -> activityConfig.declineCallback()
        }
        context.unregisterReceiver(this)
    }

    fun buildActivityIntent(activityConfig: TermsAndConditionsUI.Config.Activity, activityClazz: Class<*>) = Intent(context, activityClazz).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra("enableTilt", DeviceUtils.isSmartGlass)
        putExtra(EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION, TermsAndConditions(activityConfig.title, activityConfig.message, activityConfig.acceptText, activityConfig.declineText))
    }
}