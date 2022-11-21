package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal data class AudioOutputUiState(
    val audioDeviceList: ImmutableList<AudioDevice> = ImmutableList(emptyList()),
    val playingDeviceId: String? = null,
    override val userMessage: String? = null
) : UiState