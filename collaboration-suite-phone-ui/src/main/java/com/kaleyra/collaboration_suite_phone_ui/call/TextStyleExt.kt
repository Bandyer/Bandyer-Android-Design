package com.kaleyra.collaboration_suite_phone_ui.call

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle

private val ShadowColor = Color.Black.copy(alpha = .5f)

internal fun TextStyle.shadow() =
    copy(
        shadow = Shadow(
            color = ShadowColor,
            offset = Offset(x = -2f, y = 2f),
            blurRadius = 0.1f
        )
    )
