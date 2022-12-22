package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.runtime.Immutable

@Immutable
data class CallInfoUi(
    val title: String,
    val subtitle: String? = null,
    val watermark: Watermark,
    val isRecording: Boolean = false
)