package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_utils.ContextRetainer

interface TermsAndConditionsActivityDelegate {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_ACCEPT_TEXT = "acceptText"
        const val EXTRA_DECLINE_TEXT = "declineText"
        const val EXTRA_ACCEPT_CALLBACK = "acceptCallBack"
        const val EXTRA_DECLINE_CALLBACK = "declineCallback"
    }

    fun showActivity(activityConfig: TermsAndConditionsConfig.ActivityConfig, activityClazz: Class<*>) {
        ContextRetainer.context.startActivity(buildActivityIntent(activityConfig, activityClazz))
    }

    fun dismissActivity() {

    }

    fun buildActivityIntent(activityConfig: TermsAndConditionsConfig.ActivityConfig, activityClazz: Class<*>) = Intent(
        ContextRetainer.context, activityClazz).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra("enableTilt", DeviceUtils.isSmartGlass)
        putExtra(EXTRA_TITLE, activityConfig.title)
        putExtra(EXTRA_MESSAGE, activityConfig.message)
        putExtra(EXTRA_ACCEPT_TEXT, activityConfig.acceptText)
        putExtra(EXTRA_DECLINE_TEXT, activityConfig.declineText)
        putExtra(EXTRA_ACCEPT_CALLBACK, ParcelableLambda(activityConfig.acceptCallback))
        putExtra(EXTRA_DECLINE_CALLBACK, ParcelableLambda(activityConfig.declineCallback))
    }
}