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

package com.kaleyra.video_common_ui.termsandconditions.activity

import android.content.Context
import android.content.Intent
import com.kaleyra.video_common_ui.termsandconditions.TermsAndConditionsUI
import com.kaleyra.video_common_ui.termsandconditions.constants.Constants.EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION
import com.kaleyra.video_common_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.video_common_ui.utils.DeviceUtils

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