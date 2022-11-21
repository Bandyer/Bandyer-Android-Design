package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState

internal class WhiteboardViewModel : BaseViewModel<WhiteboardUiState>() {
    override fun initialState() = WhiteboardUiState()
}