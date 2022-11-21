package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model

sealed interface WhiteboardUploadUi {
    data class Uploading(val progress: Float): WhiteboardUploadUi
    object Error: WhiteboardUploadUi
}