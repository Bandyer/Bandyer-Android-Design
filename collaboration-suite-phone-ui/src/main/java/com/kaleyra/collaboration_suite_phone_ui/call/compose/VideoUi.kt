package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.view.View
import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class VideoUi(
    val id: String,
    val view: View?,
    val isEnabled: Boolean
    val pointers: ImmutableList<PointerUi> = ImmutableList(emptyList())
)