package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.runtime.Immutable

@Immutable
data class WatermarkInfo(
    val text: String? = null,
    val logo: Logo? = null
)
