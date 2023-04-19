package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class VideoUi(
    val id: String,
    val view: ImmutableView? = null,
    val isEnabled: Boolean = false,
    val pointers: ImmutableList<PointerUi> = ImmutableList(emptyList())
)