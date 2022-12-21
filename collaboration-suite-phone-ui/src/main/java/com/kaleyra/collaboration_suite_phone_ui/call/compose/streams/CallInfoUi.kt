package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter

@Immutable
data class CallInfoUi(
    val headerTitle: String,
    val headerSubtitle: String? = null,
    val watermarkImage: Painter? = null,
    val watermarkText: String? = null
)