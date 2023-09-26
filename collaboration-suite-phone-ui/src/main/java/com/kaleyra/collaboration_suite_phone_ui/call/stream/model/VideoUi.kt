package com.kaleyra.collaboration_suite_phone_ui.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.pointer.model.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

@Immutable
data class VideoUi(
    val id: String,
    val view: ImmutableView? = null,
    val isEnabled: Boolean = false,
    val isScreenShare: Boolean = false,
    val pointers: ImmutableList<PointerUi> = ImmutableList(emptyList())
)