package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.annotation.DrawableRes

data class Watermark(
    @DrawableRes val image: Int? = null,
    val text: String? = null,
)