package com.kaleyra.video_sdk.call.precall.model

import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

interface PreCallUiState<out T> : UiState where T: PreCallUiState<T> {

    val video: VideoUi?

    val avatar: ImmutableUri?

    val participants: ImmutableList<String>

    val watermarkInfo: WatermarkInfo?

    val isLink: Boolean

    val isConnecting: Boolean

    val isVideoIncoming: Boolean

    fun clone(
        video: VideoUi? = this@PreCallUiState.video,
        avatar: ImmutableUri? = this@PreCallUiState.avatar,
        participants: ImmutableList<String> = this@PreCallUiState.participants,
        watermarkInfo: WatermarkInfo? = this@PreCallUiState.watermarkInfo,
        isLink: Boolean = this@PreCallUiState.isLink,
        isConnecting: Boolean = this@PreCallUiState.isConnecting,
        isVideoIncoming: Boolean = this@PreCallUiState.isVideoIncoming
    ): T
}
