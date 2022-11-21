package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareState

internal class ScreenShareViewModel : BaseViewModel<ScreenShareState>() {
    override fun initialState() = ScreenShareState()
}