package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.video_networking.connector.Connector.State
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.plus

internal class TermsAndConditionsRequester(
    private val activityClazz: Class<*>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO) + CoroutineName("TermsRequester")
) {

    fun setUp(
        state: StateFlow<State>,
        onDecline: () -> Unit
    ) {
        dispose()
        state
            .onEach { connectorState ->
                if (connectorState !is State.Connecting.TermsAgreementRequired) return@onEach
                showTermsAndConditions(connectorState.requiredTerms[0], onDecline)
            }
            .takeWhile { it !is State.Connected }
            .launchIn(scope)
    }

    fun dispose() {
        scope.coroutineContext.cancelChildren()
    }

    private fun showTermsAndConditions(
        requiredTerms: State.Connecting.TermsAgreementRequired.SessionTerms,
        onDecline: () -> Unit
    ) {
        val context = ContextRetainer.context
        val notificationConfig = TermsAndConditionsUI.Config.Notification(
            title = context.getString(R.string.kaleyra_user_data_consent_agreement_notification_title),
            message = context.getString(R.string.kaleyra_user_data_consent_agreement_notification_message),
            dismissCallback = onDecline,
            enableFullscreen = DeviceUtils.isSmartGlass
        )
        val activityConfig =
            TermsAndConditionsUI.Config.Activity(
                title = requiredTerms.titleFieldText,
                message = requiredTerms.bodyFieldText,
                acceptText = requiredTerms.agreeButtonText,
                declineText = requiredTerms.disagreeButtonText,
                acceptCallback = {
                    requiredTerms.agree()
                },
                declineCallback = onDecline
            )

        TermsAndConditionsUI(activityClazz = activityClazz, notificationConfig = notificationConfig, activityConfig = activityConfig).show()
    }

}