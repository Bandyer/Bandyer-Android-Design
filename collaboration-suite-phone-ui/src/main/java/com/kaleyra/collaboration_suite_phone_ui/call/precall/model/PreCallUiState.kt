package com.kaleyra.collaboration_suite_phone_ui.call.precall.model

import com.kaleyra.collaboration_suite_phone_ui.call.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

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
