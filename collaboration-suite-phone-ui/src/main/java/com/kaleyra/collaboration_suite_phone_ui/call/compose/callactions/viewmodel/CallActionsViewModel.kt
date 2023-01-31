package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.requestCameraPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.requestMicPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.toCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.toMe
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class CallActionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallActionsUiState>(configure) {
    override fun initialState() = CallActionsUiState()

    private val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    private val callActions = call
        .toCallActions()
        .shareInEagerly(viewModelScope)

    private val isCameraEnabled = call
        .toMe()
        .isCameraEnabled()
        .shareInEagerly(viewModelScope)

    private val isMicEnabled = call
        .toMe()
        .isMicEnabled()
        .shareInEagerly(viewModelScope)

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    init {

        viewModelScope.launch {
            val actions = callActions.first()
            _uiState.update { it.copy(actionList = ImmutableList(actions)) }

            // TODO update audio action device

            // TODO check that only the modified call action will be updated ui side

            combine(
                callActions,
                isCameraEnabled,
                isMicEnabled
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
            }.launchIn(this)
        }
    }

    fun requestMicrophonePermission(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.requestMicPermission(context)
        }
    }

    fun requestCameraPermission(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.requestCameraPermission(context)
        }
    }

    fun toggleMic() {
        val input = availableInputs?.firstOrNull { it is Input.Audio }
        val isMicEnabled = isMicEnabled.getValue() ?: return
        if (!isMicEnabled) input?.tryEnable() else input?.tryDisable()
    }

    fun toggleCamera() {
        val input = availableInputs?.firstOrNull { it is Input.Video.Camera.Internal }
        val isCameraEnabled = isMicEnabled.getValue() ?: return
        if (!isCameraEnabled) input?.tryEnable() else input?.tryDisable()
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

    fun showChat(context: Context) {
        val chatBox = chatBox.getValue()
        val call = call.getValue()
        val participants = call?.participants?.getValue()
        if (chatBox == null || participants == null) return
        chatBox.chat(
            context = context,
            userIDs = participants.others.map { it.userId }
        )
    }

    fun stopScreenShare(): Boolean {
        val screenShareInputs = availableInputs?.filter { it is Input.Video.Screen || it is Input.Video.Application }
        val enabledInput = screenShareInputs?.firstOrNull { it.enabled.value }
        val isScreenShareDisabled = enabledInput?.tryDisable() ?: false
        if (isScreenShareDisabled) {
            toggleCamera()
        }
        return isScreenShareDisabled
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CallActionsViewModel(configure) as T
            }
        }
    }
}