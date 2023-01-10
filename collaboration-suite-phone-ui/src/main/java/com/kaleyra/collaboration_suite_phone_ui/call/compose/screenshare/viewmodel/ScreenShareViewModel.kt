package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal class ScreenShareViewModel(configure: suspend () -> Configuration) : BaseViewModel<ScreenShareUiState>(configure) {
//    override fun initialState() = ScreenShareUiState()
    override fun initialState() = ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)))
}