package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
data class VideoUi(
    val id: String,
    val view: ImmutableView? = null,
    val isEnabled: Boolean = false,
    val isScreenShare: Boolean = false,
    val pointers: ImmutableList<PointerUi> = ImmutableList(emptyList())
)