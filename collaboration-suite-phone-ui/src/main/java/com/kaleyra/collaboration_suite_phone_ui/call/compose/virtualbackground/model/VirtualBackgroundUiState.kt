package com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class VirtualBackgroundUiState(
    val currentBackground: VirtualBackgroundUi = VirtualBackgroundUi.None,
    val backgroundList: ImmutableList<VirtualBackgroundUi> = ImmutableList(emptyList()),
    override val userMessage: String? = null
) : UiState