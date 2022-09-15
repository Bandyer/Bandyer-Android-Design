package com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

internal fun Modifier.supportRtl(): Modifier =
    composed {
        if (LocalLayoutDirection.current == LayoutDirection.Rtl) scale(-1f, -1f) else this
    }

internal fun Modifier.highlightOnFocus(interactionSource: MutableInteractionSource): Modifier =
    composed {
        val isFocused = interactionSource.collectIsFocusedAsState().value
        border(
            width = if (isFocused) 2.dp else 0.dp,
            color = if (isFocused) Color.Red else Color.Transparent
        )
    }
