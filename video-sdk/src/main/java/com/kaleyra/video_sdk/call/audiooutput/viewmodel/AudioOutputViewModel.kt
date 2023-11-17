/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.audiooutput.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.setAudioOutputDevice
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toAudioDevicesUi
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.*

internal class AudioOutputViewModel(configure: suspend () -> Configuration) : BaseViewModel<AudioOutputUiState>(configure) {

    override fun initialState() = AudioOutputUiState()

    init {
        call
            .toAudioDevicesUi()
            .onEach { audioDevices -> _uiState.update { it.copy(audioDeviceList = ImmutableList(audioDevices)) } }
            .launchIn(viewModelScope)

        call
            .toCurrentAudioDeviceUi()
            .filterNotNull()
            .onEach { currentOutputDevice -> _uiState.update { it.copy(playingDeviceId = currentOutputDevice.id) } }
            .launchIn(viewModelScope)
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
        when {
            shouldRestoreParticipantsAudio(device) -> disableParticipantsAudio(disable = false)
            shouldMuteParticipantsAudio(device) -> disableParticipantsAudio(disable = true)
        }
        _uiState.update { it.copy(playingDeviceId = device.id) }
        call.setAudioOutputDevice(outputDevice)
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