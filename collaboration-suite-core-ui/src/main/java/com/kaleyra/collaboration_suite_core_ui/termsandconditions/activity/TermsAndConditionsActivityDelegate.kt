package com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsActivityDecorator.Companion.EXTRA_ACCEPT_TEXT
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsActivityDecorator.Companion.EXTRA_DECLINE_TEXT
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsActivityDecorator.Companion.EXTRA_MESSAGE
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsActivityDecorator.Companion.EXTRA_TITLE
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditionsUIConfig
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_utils.ContextRetainer

class TermsAndConditionsActivityDelegate(
    private val activityConfig: TermsAndConditionsUIConfig.ActivityConfig,
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

    fun buildActivityIntent(activityConfig: TermsAndConditionsUIConfig.ActivityConfig, activityClazz: Class<*>) = Intent(context, activityClazz).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra("enableTilt", DeviceUtils.isSmartGlass)
        putExtra(EXTRA_TITLE, activityConfig.title)
        putExtra(EXTRA_MESSAGE, activityConfig.message)
        putExtra(EXTRA_ACCEPT_TEXT, activityConfig.acceptText)
        putExtra(EXTRA_DECLINE_TEXT, activityConfig.declineText)
    }
}