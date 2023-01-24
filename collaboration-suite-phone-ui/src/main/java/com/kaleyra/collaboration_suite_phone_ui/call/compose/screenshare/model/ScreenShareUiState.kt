package com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal data class ScreenShareUiState(
    val targetList: ImmutableList<ScreenShareTargetUi> = ImmutableList(
        listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)
    ),
    override val userMessage: String? = null
) : UiState