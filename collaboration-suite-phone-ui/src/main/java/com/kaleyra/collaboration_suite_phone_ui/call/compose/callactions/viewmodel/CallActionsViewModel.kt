package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallExtensions.startMicrophone
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallActionsMapper.toCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioMapper.toCurrentAudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class CallActionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallActionsUiState>(configure) {
    override fun initialState() = CallActionsUiState()

    private val call = phoneBox
        .flatMapLatest { it.call }
        .shareInEagerly(viewModelScope)

    private val callActions = call
        .toCallActions()
        .shareInEagerly(viewModelScope)
    
    private val isCallConnected = call
        .flatMapLatest { it.state }
        .map { it is Call.State.Connected }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val isMyCameraEnabled = call
        .isMyCameraEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val isMyMicEnabled = call
        .isMyMicEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val currentAudioOutput = call
        .toCurrentAudioDeviceUi()
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AudioDeviceUi.Muted)

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    init {
        // TODO check that only the modified call action will be updated ui side

        combine(
            callActions,
            isCallConnected,
            isMyCameraEnabled,
            isMyMicEnabled,
            currentAudioOutput
        ) { callActions, isCallConnected, isMyCameraEnabled, isMyMicEnabled, currentAudioOutput ->
            val newActions = callActions
                .updateActionIfExists(CallAction.Microphone(isToggled = !isMyMicEnabled))
                .updateActionIfExists(CallAction.Camera(isToggled = !isMyCameraEnabled))
                .updateActionIfExists(CallAction.Audio(device = currentAudioOutput))
                .updateActionIfExists(CallAction.FileShare(isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.ScreenShare(isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.Whiteboard(isEnabled = isCallConnected))
            _uiState.update { it.copy(actionList = ImmutableList(newActions)) }
        }.launchIn(viewModelScope)
    }

    fun startMicrophone(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.startMicrophone(context)
        }
    }

    fun startCamera(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.startCamera(context)
        }
    }

    fun toggleMic() {
        val input = availableInputs?.firstOrNull { it is Input.Audio }
        if (!isMyMicEnabled.value) input?.tryEnable() else input?.tryDisable()
    }

    fun toggleCamera() {
        val input = availableInputs?.firstOrNull { it is Input.Video.Camera.Internal }
        if (!isMyCameraEnabled.value) input?.tryEnable() else input?.tryDisable()
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
        return enabledInput?.tryDisable() ?: false
    }

    private fun List<CallAction>.updateActionIfExists(action: CallAction): List<CallAction> {
        val index = indexOfFirst { it.javaClass == action.javaClass}.takeIf { it != -1 } ?: return this
        return if (this[index] == action) this else toMutableList().apply { this[index] = action }
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