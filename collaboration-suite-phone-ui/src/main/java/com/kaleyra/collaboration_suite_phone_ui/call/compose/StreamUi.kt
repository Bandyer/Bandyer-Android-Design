package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Immutable

@Immutable
data class StreamUi(
    val id: String,
    val video: VideoUi?,
    val username: String,
    val avatar: ImmutableUri?
)