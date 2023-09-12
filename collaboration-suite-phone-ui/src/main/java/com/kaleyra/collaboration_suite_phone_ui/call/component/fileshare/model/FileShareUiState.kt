package com.kaleyra.collaboration_suite_phone_ui.call.component.fileshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.uistate.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
internal data class FileShareUiState(
    val sharedFiles: ImmutableList<SharedFileUi> = ImmutableList(emptyList()),
    val showFileSizeLimit: Boolean = false
) : UiState
