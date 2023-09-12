package com.kaleyra.collaboration_suite_phone_ui.call

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri

@Immutable
data class StreamUi(
    val id: String,
    val username: String,
    val video: VideoUi? = null,
    val avatar: ImmutableUri? = null
)