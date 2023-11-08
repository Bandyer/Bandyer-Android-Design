package com.kaleyra.video_sdk.call.whiteboard.model

import android.view.View
import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
internal data class WhiteboardUiState(
    val whiteboardView: View? = null,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val upload: WhiteboardUploadUi? = null,
    val text: String? = null
) : UiState
