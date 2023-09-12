package com.kaleyra.collaboration_suite_phone_ui.chat.utility

import androidx.annotation.FloatRange
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.utils.LayoutCoordinatesExtensions.findRoot
import kotlin.math.max
import kotlin.math.min

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
            .graphicsLayer {
                this.alpha = alpha
            }
    }

@Stable
internal fun Modifier.horizontalSystemBarsPadding(): Modifier =
    composed { windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)) }

@Stable
internal fun Modifier.horizontalCutoutPadding(): Modifier =
    composed { windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)) }


@Stable
internal fun Modifier.pulse(durationMillis: Int = 1000): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        )
    )
    graphicsLayer {
        this.alpha = alpha
    }
}

internal fun Modifier.verticalGradientScrim(
    color: Color,
    @FloatRange(from = 0.0, to = 1.0) startYPercentage: Float = 0f,
    @FloatRange(from = 0.0, to = 1.0) endYPercentage: Float = 1f
): Modifier = composed {
    val colors = remember(color) {
        listOf(color.copy(alpha = 0f), color)
    }

    val brush = remember(colors, startYPercentage, endYPercentage) {
        Brush.verticalGradient(
            colors = if (startYPercentage < endYPercentage) colors else colors.reversed(),
        )
    }

    drawBehind {
        val topLeft = Offset(0f, size.height * min(startYPercentage, endYPercentage))
        val bottomRight = Offset(size.width, size.height * max(startYPercentage, endYPercentage))

        drawRect(
            topLeft = topLeft,
            size = Rect(topLeft, bottomRight).size,
            brush = brush
        )
    }
}