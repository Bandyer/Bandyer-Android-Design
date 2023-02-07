package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState

@Immutable
internal data class WhiteboardUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val upload: WhiteboardUploadUi? = null,
    val text: String? = null,
    override val userMessage: String? = null
) : UiState

