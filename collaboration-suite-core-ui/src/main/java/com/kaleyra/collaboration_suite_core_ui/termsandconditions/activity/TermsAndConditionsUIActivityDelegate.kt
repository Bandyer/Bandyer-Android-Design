package com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.TermsAndConditionsUI
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.constants.Constants.EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils

internal class TermsAndConditionsUIActivityDelegate(
    private val context: Context,
    private val activityConfig: TermsAndConditionsUI.Config.Activity,
    private val activityClazz: Class<*>
) {

    fun showActivity() {
        context.startActivity(buildActivityIntent(context, activityConfig, activityClazz))
    }

    fun getActivityIntent() = buildActivityIntent(context, activityConfig, activityClazz)

    private fun buildActivityIntent(context: Context, activityConfig: TermsAndConditionsUI.Config.Activity, activityClazz: Class<*>) = Intent(context, activityClazz).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra("enableTilt", DeviceUtils.isSmartGlass)
        putExtra(EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION, TermsAndConditions(activityConfig.title, activityConfig.message, activityConfig.acceptText, activityConfig.declineText))
    }

}