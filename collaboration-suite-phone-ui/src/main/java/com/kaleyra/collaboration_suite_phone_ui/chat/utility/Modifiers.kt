package com.kaleyra.collaboration_suite_phone_ui.chat.utility

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.findRoot

private val FocusHighlightStroke = 2.dp
private val FocusHighlightColor = Color.Red

private const val FadeVisibilityThreshold = 0.15

@Stable
internal fun Modifier.supportRtl(): Modifier =
    composed {
        if (LocalLayoutDirection.current == LayoutDirection.Rtl) scale(-1f, -1f) else this
    }

@Stable
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

@Stable
internal fun Modifier.fadeBelowOfRootBottomBound(): Modifier =
    composed {
        val navigationBarsPadding = with(LocalDensity.current) {
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().toPx()
        }
        var alpha by remember { mutableStateOf(0f) }
        this
            .onGloballyPositioned { layoutCoordinates ->
                val rootHeight = layoutCoordinates.findRoot().size.height
                val boundsInRoot = layoutCoordinates.boundsInRoot()
                val height = layoutCoordinates.size.height.toFloat()
                val out = (boundsInRoot.bottom - rootHeight + navigationBarsPadding).coerceIn(0f, height)
                alpha = (1 - out / height).takeIf { it > FadeVisibilityThreshold } ?: 0f
            }
            .alpha(alpha)
    }

@Stable
internal fun Modifier.horizontalInsetsPadding(): Modifier =
    composed {
        val systemBars = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
        val cutout = WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)

        windowInsetsPadding(systemBars.add(cutout))
    }


