package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState

internal class ScreenShareViewModel : BaseViewModel<ScreenShareUiState>() {
    override fun initialState() = ScreenShareUiState()
}