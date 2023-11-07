package com.kaleyra.video_sdk.call.whiteboard.model

import androidx.compose.runtime.Stable

@Stable
sealed interface WhiteboardUploadUi {
    data class Uploading(val progress: Float): WhiteboardUploadUi
    object Error: WhiteboardUploadUi
}