package com.kaleyra.collaboration_suite_phone_ui.termsandconditions.model

import com.kaleyra.collaboration_suite_phone_ui.call.core.model.UiState

data class TermsAndConditionsUiState(
    val isConnected: Boolean = false,
    val isDeclined: Boolean = false
) : UiState