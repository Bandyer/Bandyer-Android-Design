package com.kaleyra.video_sdk.call.fileshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
internal data class FileShareUiState(
    val sharedFiles: ImmutableList<SharedFileUi> = ImmutableList(emptyList()),
    val showFileSizeLimit: Boolean = false
) : UiState
