package com.kaleyra.collaboration_suite_phone_ui.call.compose

import kotlinx.coroutines.flow.StateFlow

interface CallUiViewModel {

    val uiState: StateFlow<CallUiState>
}