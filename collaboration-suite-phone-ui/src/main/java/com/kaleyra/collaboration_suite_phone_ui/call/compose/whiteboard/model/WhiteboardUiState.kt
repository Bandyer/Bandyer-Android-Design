package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState

internal data class WhiteboardUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val upload: WhiteboardUploadUi? = null,
    override val userMessage: String? = null
) : UiState