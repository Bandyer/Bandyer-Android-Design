package com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.setAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.AudioOutputMapper.toAudioDevicesUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class AudioOutputViewModel(configure: suspend () -> Configuration) : BaseViewModel<AudioOutputUiState>(configure) {

    override fun initialState() = AudioOutputUiState()

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

    suspend fun setDevice(device: AudioDeviceUi) {
        val call = call.getValue()
        val devices = call?.audioOutputDevicesList?.getValue() ?: return
        val outputDevice = when (device) {
            is AudioDeviceUi.Bluetooth -> devices.first { it is AudioOutputDevice.Bluetooth && it.identifier == device.id }
            AudioDeviceUi.EarPiece -> AudioOutputDevice.Earpiece()
            AudioDeviceUi.LoudSpeaker -> AudioOutputDevice.Loudspeaker()
            AudioDeviceUi.Muted -> AudioOutputDevice.None()
            AudioDeviceUi.WiredHeadset -> AudioOutputDevice.WiredHeadset()
        }
        when {
            shouldRestoreParticipantsAudio(device) -> disableParticipantsAudio(disable = false)
            shouldMuteParticipantsAudio(device) -> disableParticipantsAudio(disable = true)
        }
        _uiState.update { it.copy(playingDeviceId = device.id) }
        call.setAudioOutputDevice(outputDevice)
        call.currentAudioOutputDevice.filterNotNull().first { it::class == outputDevice::class }
    }

    private fun shouldRestoreParticipantsAudio(selectedDevice: AudioDeviceUi) = uiState.value.playingDeviceId == AudioDeviceUi.Muted.id && selectedDevice.id != AudioDeviceUi.Muted.id

    private fun shouldMuteParticipantsAudio(selectedDevice: AudioDeviceUi) = uiState.value.playingDeviceId != AudioDeviceUi.Muted.id && selectedDevice.id == AudioDeviceUi.Muted.id

    private fun disableParticipantsAudio(disable: Boolean) {
        val call = call.getValue() ?: return
        val participants = call.participants
        val others = participants.value.others
        val streams = others.map { it.streams.value }.flatten()
        val audio = streams.map { it.audio.value }
        if (disable) audio.forEach { it?.tryDisable() }
        else audio.forEach { it?.tryEnable() }
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