package com.kaleyra.video_sdk.call.pointer.model

import androidx.compose.runtime.Immutable

@Immutable
data class PointerUi(
    val username: String,
    val x: Float,
    val y: Float
)