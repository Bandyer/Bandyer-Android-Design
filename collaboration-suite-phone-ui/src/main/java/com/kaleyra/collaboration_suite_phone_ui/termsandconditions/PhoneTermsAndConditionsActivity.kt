package com.kaleyra.collaboration_suite_phone_ui.termsandconditions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.constants.Constants
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.extensions.TermsAndConditionsExt.accept
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.extensions.TermsAndConditionsExt.decline
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.collaboration_suite_phone_ui.termsandconditions.screen.TermsAndConditionsScreen
import com.kaleyra.collaboration_suite_phone_ui.theme.TermsAndConditionsTheme

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