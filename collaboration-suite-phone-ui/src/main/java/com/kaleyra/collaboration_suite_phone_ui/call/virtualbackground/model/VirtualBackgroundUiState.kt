package com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.uistate.UiState
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

@Immutable
data class VirtualBackgroundUiState(
    val currentBackground: VirtualBackgroundUi = VirtualBackgroundUi.None,
    val backgroundList: ImmutableList<VirtualBackgroundUi> = ImmutableList(emptyList())
) : UiState