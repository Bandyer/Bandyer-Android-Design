package com.kaleyra.video_sdk.call.audiooutput.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
internal data class AudioOutputUiState(
    val audioDeviceList: ImmutableList<AudioDeviceUi> = ImmutableList(emptyList()),
    val playingDeviceId: String? = null
) : UiState