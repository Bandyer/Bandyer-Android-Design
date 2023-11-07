package com.kaleyra.video_sdk.call.screenshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
internal data class ScreenShareUiState(
    val targetList: ImmutableList<ScreenShareTargetUi> = ImmutableList(
        listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)
    )
) : UiState