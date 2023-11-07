package com.kaleyra.video_sdk.call.virtualbackground.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
data class VirtualBackgroundUiState(
    val currentBackground: VirtualBackgroundUi = VirtualBackgroundUi.None,
    val backgroundList: ImmutableList<VirtualBackgroundUi> = ImmutableList(emptyList())
) : UiState