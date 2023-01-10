package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.mockAudioDevices
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal class AudioOutputViewModel(configure: suspend () -> Configuration) : BaseViewModel<AudioOutputUiState>(configure) {
//    override fun initialState() = AudioOutputUiState()
    override fun initialState() = AudioOutputUiState(audioDeviceList = mockAudioDevices, playingDeviceId = "id")
}