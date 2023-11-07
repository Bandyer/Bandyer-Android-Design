package com.kaleyra.video_sdk.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle

internal object TextStyleExtensions {

    private val ShadowColor = Color.Black.copy(alpha = .5f)

    fun TextStyle.shadow() =
        copy(
            shadow = Shadow(
                color = ShadowColor,
                offset = Offset(x = -2f, y = 2f),
                blurRadius = 0.1f
            )
        )
}


