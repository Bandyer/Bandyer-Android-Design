package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

@Immutable
data class WatermarkInfo(
    @DrawableRes val image: Int? = null,
    val text: String? = null,
)