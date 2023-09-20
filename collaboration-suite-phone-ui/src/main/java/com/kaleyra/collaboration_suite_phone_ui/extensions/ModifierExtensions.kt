package com.kaleyra.collaboration_suite_phone_ui.extensions

import androidx.annotation.FloatRange
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.utils.LayoutCoordinatesExtensions.findRoot
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

private val FocusHighlightStroke = 2.dp
private val FocusHighlightColor = Color.Red

private const val FadeVisibilityThreshold = 0.15

internal object ModifierExtensions {

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
            val enableHighlight = remember(inputModeManager, isFocused) { derivedStateOf { inputModeManager.inputMode != InputMode.Touch && isFocused }.value }
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
            ),
            label = "pulse"
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

    /**
     * A [Modifier] that draws a border around elements that are recomposing. The border increases in
     * size and interpolates from red to green as more recompositions occur before a timeout.
     */
    @Stable
    internal fun Modifier.recomposeHighlighter(): Modifier = this.then(recomposeModifier)

    // Use a single instance + @Stable to ensure that recompositions can enable skipping optimizations
    // Modifier.composed will still remember unique data per call site.
    private val recomposeModifier =
        Modifier.composed(inspectorInfo = debugInspectorInfo { name = "recomposeHighlighter" }) {
            // The total number of compositions that have occurred. We're not using a State<> here be
            // able to read/write the value without invalidating (which would cause infinite
            // recomposition).
            val totalCompositions = remember { arrayOf(0L) }
            totalCompositions[0]++

            // The value of totalCompositions at the last timeout.
            val totalCompositionsAtLastTimeout = remember { mutableStateOf(0L) }

            // Start the timeout, and reset everytime there's a recomposition. (Using totalCompositions
            // as the key is really just to cause the timer to restart every composition).
            LaunchedEffect(totalCompositions[0]) {
                delay(3000)
                totalCompositionsAtLastTimeout.value = totalCompositions[0]
            }

            Modifier.drawWithCache {
                onDrawWithContent {
                    // Draw actual content.
                    drawContent()

                    // Below is to draw the highlight, if necessary. A lot of the logic is copied from
                    // Modifier.border
                    val numCompositionsSinceTimeout =
                        totalCompositions[0] - totalCompositionsAtLastTimeout.value

                    val hasValidBorderParams = size.minDimension > 0f
                    if (!hasValidBorderParams || numCompositionsSinceTimeout <= 0) {
                        return@onDrawWithContent
                    }

                    val (color, strokeWidthPx) =
                        when (numCompositionsSinceTimeout) {
                            // We need at least one composition to draw, so draw the smallest border
                            // color in blue.
                            1L -> Color.Blue to 1f
                            // 2 compositions is _probably_ okay.
                            2L -> Color.Green to 2.dp.toPx()
                            // 3 or more compositions before timeout may indicate an issue. lerp the
                            // color from yellow to red, and continually increase the border size.
                            else -> {
                                lerp(
                                    Color.Yellow.copy(alpha = 0.8f),
                                    Color.Red.copy(alpha = 0.5f),
                                    min(1f, (numCompositionsSinceTimeout - 1).toFloat() / 100f)
                                ) to numCompositionsSinceTimeout.toInt().dp.toPx()
                            }
                        }

                    val halfStroke = strokeWidthPx / 2
                    val topLeft = Offset(halfStroke, halfStroke)
                    val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

                    val fillArea = (strokeWidthPx * 2) > size.minDimension
                    val rectTopLeft = if (fillArea) Offset.Zero else topLeft
                    val size = if (fillArea) size else borderSize
                    val style = if (fillArea) Fill else Stroke(strokeWidthPx)

                    drawRect(
                        brush = SolidColor(color),
                        topLeft = rectTopLeft,
                        size = size,
                        style = style
                    )
                }
            }
        }
}