package com.kaleyra.video_sdk.termsandconditions.model

import com.kaleyra.video_sdk.common.uistate.UiState

data class TermsAndConditionsUiState(
    val isConnected: Boolean = false,
    val isDeclined: Boolean = false
) : UiState