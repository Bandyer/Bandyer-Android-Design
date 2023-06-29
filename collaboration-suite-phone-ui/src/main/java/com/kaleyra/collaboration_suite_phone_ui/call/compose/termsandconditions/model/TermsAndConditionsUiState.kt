package com.kaleyra.collaboration_suite_phone_ui.call.compose.termsandconditions.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState

data class TermsAndConditionsUiState(
    val isConnected: Boolean = false,
    val isDeclined: Boolean = false
) : UiState