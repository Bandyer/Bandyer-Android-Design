package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.toCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*

internal class CallActionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallActionsUiState>(configure) {
    override fun initialState() = CallActionsUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    init {
        val me = call.flatMapLatest { it.participants }.map { it.me }

        // TODO check that only the modified call action will be updated ui side
        combine(
            call.toCallActions(),
            me.isCameraEnabled(),
            me.isMicEnabled()
        ) { callActions, isCameraEnabled, isMicEnabled ->
            val actionList = callActions.toMutableList()

            val cameraIndex = actionList.indexOfFirst { it is CallAction.Camera }
            val micIndex = actionList.indexOfFirst { it is CallAction.Microphone }

            if (cameraIndex != -1) {
                actionList[cameraIndex] = CallAction.Camera(isToggled = !isCameraEnabled)
            }
            if (micIndex != -1) {
                actionList[micIndex] = CallAction.Microphone(isToggled = !isMicEnabled)
            }

            _uiState.update { it.copy(actionList = ImmutableList(actionList)) }
        }.launchIn(viewModelScope)
    }

    fun enableMicrophone(enable: Boolean) {
        val input = availableInputs?.firstOrNull { it is Input.Audio } ?: return
        if (enable) input.tryEnable() else input.tryDisable()
    }

    fun enableCamera(enable: Boolean) {
        val input = availableInputs?.firstOrNull { it is Input.Video.Camera.Internal } ?: return
        if (enable) input.tryEnable() else input.tryDisable()
    }

    fun switchCamera() {
        val camera = availableInputs?.filterIsInstance<Input.Video.Camera.Internal>()?.firstOrNull()
        val currentLens = camera?.currentLens?.value
        val newLens = camera?.lenses?.firstOrNull { it.isRear != currentLens?.isRear } ?: return
        camera.setLens(newLens)
    }

    fun hangUp() {
        call.getValue()?.end()
    }
}