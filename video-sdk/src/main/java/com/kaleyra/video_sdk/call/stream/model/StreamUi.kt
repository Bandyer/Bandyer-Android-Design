package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

@Immutable
data class StreamUi(
    val id: String,
    val username: String,
    val video: VideoUi? = null,
    val avatar: ImmutableUri? = null
)