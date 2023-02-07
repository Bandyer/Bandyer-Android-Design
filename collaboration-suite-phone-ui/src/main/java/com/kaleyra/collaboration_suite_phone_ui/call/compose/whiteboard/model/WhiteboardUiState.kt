package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model

import android.webkit.WebView
import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState

@Immutable
internal data class WhiteboardUiState(
    val webView: WebView? = null,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val upload: WhiteboardUploadUi? = null,
    override val userMessage: String? = null
) : UiState