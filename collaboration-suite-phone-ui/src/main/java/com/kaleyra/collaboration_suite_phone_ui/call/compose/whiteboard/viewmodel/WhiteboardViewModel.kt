package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState

internal class WhiteboardViewModel(configure: suspend () -> Configuration) : BaseViewModel<WhiteboardUiState>(configure) {
//    override fun initialState() = WhiteboardUiState()
    override fun initialState() = WhiteboardUiState(isLoading = false, isOffline = true)
}