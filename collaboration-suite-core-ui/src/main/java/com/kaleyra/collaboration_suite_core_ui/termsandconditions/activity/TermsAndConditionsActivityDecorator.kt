package com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsActivityDelegate.Companion.ACTION_ACCEPT
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsActivityDelegate.Companion.ACTION_DECLINE

interface TermsAndConditionsActivityDecorator {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_ACCEPT_TEXT = "acceptText"
        const val EXTRA_DECLINE_TEXT = "declineText"
    }

    fun onConfig(title: String, message: String, acceptText: String, declineText: String)

    fun onAcceptTerms(context: Context) = context.sendBroadcast(buildBroadcastIntent(context, ACTION_ACCEPT))

    fun onDeclineTerms(context: Context) = context.sendBroadcast(buildBroadcastIntent(context, ACTION_DECLINE))

    fun getConfigFromIntent(intent: Intent) {
        val extras = intent.extras ?: return
        val title = extras.getString(EXTRA_TITLE, "")
        val message = extras.getString(EXTRA_MESSAGE, "")
        val acceptText = extras.getString(EXTRA_ACCEPT_TEXT, "")
        val declineText = extras.getString(EXTRA_DECLINE_TEXT, "")
        onConfig(title, message, acceptText, declineText)
    }

    private fun buildBroadcastIntent(context: Context, action: String): Intent {
        return Intent(action).apply {
            this.`package` = context.applicationContext.packageName
        }
    }

}