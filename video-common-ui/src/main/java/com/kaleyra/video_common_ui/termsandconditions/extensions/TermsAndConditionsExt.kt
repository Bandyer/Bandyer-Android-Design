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
