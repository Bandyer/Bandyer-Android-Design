package com.kaleyra.collaboration_suite_phone_ui.call.compose.termsandconditions.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages

data class TermsAndConditionsUiState(
    val isConnected: Boolean = false,
    val isDeclined: Boolean = false,
    override val userMessages: UserMessages = UserMessages(),
) : UiState