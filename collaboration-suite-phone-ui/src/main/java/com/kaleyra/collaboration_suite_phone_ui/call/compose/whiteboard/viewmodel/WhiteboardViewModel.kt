package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState

internal class WhiteboardViewModel : BaseViewModel<WhiteboardUiState>() {
//    override fun initialState() = WhiteboardUiState()
    override fun initialState() = WhiteboardUiState(isLoading = false, isOffline = true)
}