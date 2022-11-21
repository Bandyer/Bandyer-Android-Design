package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal data class AudioOutputUiState(
    val audioDeviceList: ImmutableList<AudioDeviceUi> = ImmutableList(emptyList()),
    val playingDeviceId: String? = null,
    override val userMessage: String? = null
) : UiState