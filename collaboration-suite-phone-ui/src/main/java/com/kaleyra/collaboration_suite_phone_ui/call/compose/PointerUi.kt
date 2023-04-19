package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Immutable

@Immutable
data class PointerUi(
    val username: String,
    val x: Float,
    val y: Float
)