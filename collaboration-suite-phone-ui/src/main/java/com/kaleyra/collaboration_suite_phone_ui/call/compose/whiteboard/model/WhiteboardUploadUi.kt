package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface WhiteboardUploadUi {
    data class Uploading(val progress: Float): WhiteboardUploadUi
    object Error: WhiteboardUploadUi
}