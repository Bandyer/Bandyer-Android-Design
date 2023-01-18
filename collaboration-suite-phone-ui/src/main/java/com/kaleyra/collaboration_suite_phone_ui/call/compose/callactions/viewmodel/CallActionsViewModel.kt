package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.CallActionsMappers.isAudioEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.CallActionsMappers.isCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*

internal class CallActionsViewModel(configure: suspend () -> Configuration) :
    BaseViewModel<CallActionsUiState>(configure) {
    override fun initialState() = CallActionsUiState()

    val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    init {
        val me = call.flatMapLatest { it.participants }.map { it.me }

        me
            .isCameraEnabled()
            .onEach { isEnabled ->
                _uiState.update { uiState ->
                    val actionList = uiState.actionList.value.toMutableList()
                    val cameraIndex = actionList.indexOfFirst { it is CallAction.Camera }
                    actionList[cameraIndex] = CallAction.Camera(isEnabled = isEnabled)
                    uiState.copy(actionList = ImmutableList(actionList))
                }
            }
            .launchIn(viewModelScope)

        me
            .isAudioEnabled()
            .onEach { }
            .launchIn(viewModelScope)
    }

    fun enableMicrophone(enable: Boolean) {
        val input = availableInputs?.filterIsInstance<Input.Audio>()?.firstOrNull() ?: return
        if (enable) input.tryEnable() else input.tryDisable()
    }

    fun enableCamera(enable: Boolean) {
        val input = availableInputs?.filterIsInstance<Input.Video.Camera.Internal>()?.firstOrNull() ?: return
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