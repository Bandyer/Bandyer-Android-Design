package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.utils.FlowUtils.combine
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallActionsMapper.toCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasUsbCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isSharingScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.isVirtualBackgroundEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.CameraRestrictionMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class CallActionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallActionsUiState>(configure) {
    override fun initialState() = CallActionsUiState()

    private val callActions = call
        .toCallActions(company.flatMapLatest { it.id })
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

    private val isSharingScreen = call
        .isSharingScreen()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val hasUsbCamera = call
        .hasUsbCamera()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val currentAudioOutput = call
        .toCurrentAudioDeviceUi()
        .filterNotNull()
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.Eagerly, AudioDeviceUi.Muted)

    private val isVirtualBackgroundEnabled = call
        .isVirtualBackgroundEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    private var wasCameraRestrictionMessageSent = false

    init {
        // TODO check that only the modified call action will be updated ui side

        combine(
            callActions,
            isCallConnected,
            isMyCameraEnabled,
            isMyMicEnabled,
            isSharingScreen,
            currentAudioOutput,
            isVirtualBackgroundEnabled,
            hasUsbCamera
        ) { callActions, isCallConnected, isMyCameraEnabled, isMyMicEnabled, isSharingScreen, currentAudioOutput, isVirtualBackgroundEnabled, hasUsbCamera ->
            callActions
                .updateActionIfExists(CallAction.Microphone(isToggled = !isMyMicEnabled))
                .updateActionIfExists(CallAction.Camera(isToggled = !isMyCameraEnabled))
                .updateActionIfExists(CallAction.Audio(device = currentAudioOutput))
                .updateActionIfExists(CallAction.FileShare(isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.ScreenShare(isToggled = isSharingScreen, isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.VirtualBackground(isToggled = isVirtualBackgroundEnabled))
                .updateActionIfExists(CallAction.Whiteboard(isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.SwitchCamera(isEnabled = !hasUsbCamera && isMyCameraEnabled))
        }
            .distinctUntilChanged()
            .onEach { actions -> _uiState.update { it.copy(actionList = ImmutableList(actions)) } }
            .launchIn(viewModelScope)
    }

    fun startMicrophone(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.inputs?.request(context, Inputs.Type.Microphone)
        }
    }

    fun startCamera(context: FragmentActivity) {
        viewModelScope.launch {
            call.getValue()?.inputs?.request(context, Inputs.Type.Camera.Internal)
        }
    }

    fun toggleMic() {
        val input = availableInputs?.firstOrNull { it is Input.Audio }
        if (!isMyMicEnabled.value) input?.tryEnable() else input?.tryDisable()
    }

    fun toggleCamera() {
        val input = availableInputs?.let { inputs ->
            inputs.firstOrNull { it is Input.Video.Camera.Usb } ?: inputs.firstOrNull { it is Input.Video.Camera.Internal }
        }
        val call = call.getValue() ?: return
        val participants = call.participants.value
        val restrictions = participants.me.restrictions
        val canUseCamera = !restrictions.camera.value.usage
        if (canUseCamera) {
            if (!isMyCameraEnabled.value) input?.tryEnable() else input?.tryDisable()
        } else {
            // Avoid sending a burst of camera restriction message event
            if (wasCameraRestrictionMessageSent) return
            viewModelScope.launch {
                wasCameraRestrictionMessageSent = true
                CallUserMessagesProvider.sendUserMessage(CameraRestrictionMessage())
                delay(1500L)
                wasCameraRestrictionMessageSent = false
            }
        }
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
        val companyId = company.getValue()?.id?.getValue()
        chatBox.chat(
            context = context,
            userIDs = participants.others.filter { it.userId != companyId }.map { it.userId }
        )
    }

    fun tryStopScreenShare(): Boolean {
        val input = availableInputs?.filter { it is Input.Video.Screen || it is Input.Video.Application }?.firstOrNull { it.enabled.value }
        val call = call.getValue()
        return if (input == null || call == null) false
        else {
            val me = call.participants.value.me
            val streams = me.streams.value
            val stream = streams.firstOrNull { it.id == SCREEN_SHARE_STREAM_ID }
            if (stream != null) me.removeStream(stream)
            input.tryDisable() && stream != null
        }
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