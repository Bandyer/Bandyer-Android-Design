package com.kaleyra.video_sdk.call.callinfowidget.model

import androidx.compose.runtime.Immutable

@Immutable
data class WatermarkInfo(
    val text: String? = null,
    val logo: Logo? = null
)
