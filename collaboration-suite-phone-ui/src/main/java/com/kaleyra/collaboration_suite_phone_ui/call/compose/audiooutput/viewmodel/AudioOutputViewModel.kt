package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.setAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioMapper.toAudioDevicesUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioMapper.toCurrentAudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class AudioOutputViewModel(configure: suspend () -> Configuration) : BaseViewModel<AudioOutputUiState>(configure) {

    override fun initialState() = AudioOutputUiState()

    val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    init {
        call
            .toAudioDevicesUi()
            .onEach { audioDevices ->
                _uiState.update { it.copy(audioDeviceList = ImmutableList(audioDevices)) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val currentOutputDevice = call.toCurrentAudioDeviceUi().filterNotNull().first()
            _uiState.update { it.copy(playingDeviceId = currentOutputDevice.id) }
        }
    }

    fun setDevice(device: AudioDeviceUi) {
        val call = call.getValue()
        val devices = call?.audioOutputDevicesList?.getValue() ?: return
        val outputDevice = when (device) {
            is AudioDeviceUi.Bluetooth -> devices.first { it is AudioOutputDevice.Bluetooth && it.identifier == device.id }
            AudioDeviceUi.EarPiece -> AudioOutputDevice.Earpiece()
            AudioDeviceUi.LoudSpeaker -> AudioOutputDevice.Loudspeaker()
            AudioDeviceUi.Muted -> AudioOutputDevice.None()
            AudioDeviceUi.WiredHeadset -> AudioOutputDevice.WiredHeadset()
        }
        call.setAudioOutputDevice(outputDevice)
        _uiState.update { it.copy(playingDeviceId = device.id) }
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AudioOutputViewModel(configure) as T
            }
        }
    }

}