package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal class ScreenShareViewModel : BaseViewModel<ScreenShareState>() {
    override fun initialState() = ScreenShareState(targetList = ImmutableList(listOf()), userMessage = null)
}