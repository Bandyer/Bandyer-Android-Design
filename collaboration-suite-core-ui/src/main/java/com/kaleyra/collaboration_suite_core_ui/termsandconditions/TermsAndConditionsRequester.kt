package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_networking.Session
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class TermsAndConditionsRequester(
    private val activityClazz: Class<*>,
    private val onAccept: (session: Collaboration.Session) -> Unit,
    private val onDecline: () -> Unit,
    private val parentScope: CoroutineScope
) {

    private var scope: CoroutineScope? = null

    fun setUp(session: Collaboration.Session) {
        dispose()
        scope = newChildScope(parentScope)
        session.state.onEach { sessionState ->
            if (sessionState !is Session.State.Authenticating.TermsAgreementRequired) return@onEach
            showTermsAndConditions(sessionState.requiredTerms[0], session)
        }.launchIn(scope!!)
    }

    fun dispose() {
        scope?.cancel()
    }

    private fun showTermsAndConditions(
        requiredTerms: Session.State.Authenticating.TermsAgreementRequired.SessionTerms,
        session: Collaboration.Session
    ) {
        val context = ContextRetainer.context
        val notificationConfig = TermsAndConditionsUI.Config.Notification(
            title = context.getString(R.string.kaleyra_user_data_consent_agreement_notification_title),
            message = context.getString(R.string.kaleyra_user_data_consent_agreement_notification_message),
            dismissCallback = onDecline,
            enableFullscreen = DeviceUtils.isSmartGlass
        )
        val activityConfig = TermsAndConditionsUI.Config.Activity(
            title = requiredTerms.titleFieldText,
            message = requiredTerms.bodyFieldText,
            acceptText = requiredTerms.agreeButtonText,
            declineText = requiredTerms.disagreeButtonText,
            acceptCallback = {
                requiredTerms.agree()
                onAccept(session)
            },
            declineCallback = onDecline
        )
        TermsAndConditionsUI(activityClazz, notificationConfig, activityConfig).show()
    }

    private fun newChildScope(parentScope: CoroutineScope) =
        CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]) + Dispatchers.IO)

}