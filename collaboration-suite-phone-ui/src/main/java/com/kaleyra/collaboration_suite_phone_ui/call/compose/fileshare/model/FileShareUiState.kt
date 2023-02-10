package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
internal data class FileShareUiState(
    val sharedFiles: ImmutableList<SharedFileUi> = ImmutableList(emptyList()),
    override val userMessage: String? = null
) : UiState
