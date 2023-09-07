package com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
internal data class ScreenShareUiState(
    val targetList: ImmutableList<ScreenShareTargetUi> = ImmutableList(
        listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)
    )
) : UiState