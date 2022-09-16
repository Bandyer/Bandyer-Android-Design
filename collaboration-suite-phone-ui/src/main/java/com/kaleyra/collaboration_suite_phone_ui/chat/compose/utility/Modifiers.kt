package com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val FocusHighlightStroke = 2.dp
private val FocusHighlightColor = Color.Red

internal fun Modifier.supportRtl(): Modifier =
    composed {
        if (LocalLayoutDirection.current == LayoutDirection.Rtl) scale(-1f, -1f) else this
    }

internal fun Modifier.highlightOnFocus(interactionSource: MutableInteractionSource): Modifier =
    composed {
        val inputModeManager = LocalInputModeManager.current
        val isFocused = interactionSource.collectIsFocusedAsState().value
        val enableHighlight = derivedStateOf { inputModeManager.inputMode != InputMode.Touch && isFocused }.value
        border(
            width = if (enableHighlight) FocusHighlightStroke else 0.dp,
            color = if (enableHighlight) FocusHighlightColor else Color.Transparent
        )
    }
