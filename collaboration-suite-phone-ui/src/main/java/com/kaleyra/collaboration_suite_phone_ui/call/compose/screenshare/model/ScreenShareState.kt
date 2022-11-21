package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal data class ScreenShareState(
    val targetList: ImmutableList<ScreenShareTargetUi> = ImmutableList(emptyList()),
    override val userMessage: String? = null
) : UiState