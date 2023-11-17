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

package com.kaleyra.video_sdk.termsandconditions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import com.kaleyra.video_common_ui.termsandconditions.constants.Constants
import com.kaleyra.video_common_ui.termsandconditions.extensions.TermsAndConditionsExt.accept
import com.kaleyra.video_common_ui.termsandconditions.extensions.TermsAndConditionsExt.decline
import com.kaleyra.video_common_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.video_sdk.termsandconditions.screen.TermsAndConditionsScreen
import com.kaleyra.video_sdk.theme.TermsAndConditionsTheme

class PhoneTermsAndConditionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val termsAndConditions = intent.extras?.getParcelable<TermsAndConditions>(Constants.EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION)
        onBackPressedDispatcher.addCallback { termsAndConditions?.decline() }
        if (termsAndConditions != null) {
            setContent {
                TermsAndConditionsTheme {
                    TermsAndConditionsScreen(
                        title = termsAndConditions.title,
                        message = termsAndConditions.message,
                        acceptText = termsAndConditions.acceptText,
                        declineText = termsAndConditions.declineText,
                        onAccept = { termsAndConditions.accept() },
                        onDecline = { termsAndConditions.decline() }
                    )
                }
            }
        } else finishAndRemoveTask()
    }

}