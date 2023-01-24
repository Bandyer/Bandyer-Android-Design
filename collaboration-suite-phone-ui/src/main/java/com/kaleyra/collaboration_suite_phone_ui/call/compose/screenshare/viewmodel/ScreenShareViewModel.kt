package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState
import kotlinx.coroutines.flow.flatMapLatest

internal class ScreenShareViewModel(configure: suspend () -> Configuration) : BaseViewModel<ScreenShareUiState>(configure) {
    override fun initialState() = ScreenShareUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    fun shareScreen(target: ScreenShareTargetUi) {
        when(target) {
            ScreenShareTargetUi.Application -> shareApplication()
            ScreenShareTargetUi.Device -> shareDevice()
        }
    }

    private fun shareApplication() {
        val input = availableInputs?.firstOrNull { it is Input.Video.Application } ?: return
        input.tryEnable()
    }

    private fun shareDevice() {
        val input = availableInputs?.firstOrNull { it is Input.Video.Screen } ?: return
        input.tryEnable()
    }
}