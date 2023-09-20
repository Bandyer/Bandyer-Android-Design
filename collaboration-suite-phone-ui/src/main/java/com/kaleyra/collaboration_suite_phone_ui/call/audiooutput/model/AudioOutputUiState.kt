package com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.uistate.UiState
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

@Immutable
internal data class AudioOutputUiState(
    val audioDeviceList: ImmutableList<AudioDeviceUi> = ImmutableList(emptyList()),
    val playingDeviceId: String? = null
) : UiState