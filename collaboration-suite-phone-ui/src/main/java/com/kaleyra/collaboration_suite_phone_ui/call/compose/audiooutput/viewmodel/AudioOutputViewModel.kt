package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputState

internal class AudioOutputViewModel : BaseViewModel<AudioOutputState>() {
    override fun initialState() = AudioOutputState()
}