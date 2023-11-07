package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object WhiteboardMapper {

    fun Flow<CallUI>.isWhiteboardLoading(): Flow<Boolean> =
        this.map { it.whiteboard }
            .flatMapLatest { it.state }
            .map { it is Whiteboard.State.Loading }
            .distinctUntilChanged()

    fun Flow<CallUI>.getWhiteboardTextEvents(): Flow<Whiteboard.Event.Text> =
        this.map { it.whiteboard }
            .flatMapLatest { it.events }
            .filterIsInstance()

    fun SharedFile.toWhiteboardUploadUi(): Flow<WhiteboardUploadUi?> {
        return state.map { state ->
            val progress = when (state) {
                is SharedFile.State.InProgress -> (state.progress / this.size.toFloat()).coerceIn(0f, 1f)
                is SharedFile.State.Success -> 1f
                else -> 0f
            }
            val upload = when (state) {
                is SharedFile.State.Cancelled -> null
                is SharedFile.State.Error -> WhiteboardUploadUi.Error
                else -> WhiteboardUploadUi.Uploading(progress)
            }
            upload
        }.distinctUntilChanged()
    }
}