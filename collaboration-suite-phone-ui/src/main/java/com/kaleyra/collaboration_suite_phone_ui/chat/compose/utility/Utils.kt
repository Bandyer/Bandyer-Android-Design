package com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

internal fun Modifier.supportRtl(): Modifier =
    composed { if (LocalLayoutDirection.current == LayoutDirection.Rtl) scale(scaleX = -1f, scaleY = -1f) else this }