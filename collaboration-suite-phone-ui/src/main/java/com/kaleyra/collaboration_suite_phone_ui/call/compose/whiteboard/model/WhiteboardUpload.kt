package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model

sealed interface WhiteboardUpload {
    data class Uploading(val progress: Float): WhiteboardUpload
    object Error: WhiteboardUpload
}