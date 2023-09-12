package com.kaleyra.collaboration_suite_phone_ui.call.feedback.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.R

// Default slider thumb radius in the material library
private val ThumbRadius = 10.dp
private val StarSize = 28.dp
// Adjust the padding for the layout placement
private val LayoutModifier = Modifier.padding(horizontal = ThumbRadius / 2)
val StarSliderTag = "StarSliderTag"

@Composable
internal fun StarSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    levels: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thumbSize = remember { with(density) { ThumbRadius.toPx().toInt() } }
    Layout(
        modifier = modifier.then(LayoutModifier),
        content = {
            Slider(
                value = value,
                onValueChange = onValueChange,
                steps = levels - 2,
                valueRange = 1f.rangeTo(levels.toFloat()),
                modifier = Modifier.alpha(0f).testTag(StarSliderTag)
            )
            repeat(levels) { index ->
                val scale by animateFloatAsState(targetValue = if (index <= value - 1) 1f else .75f)
                Box(modifier = Modifier.size(StarSize)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_empty_star),
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_full_star),
                        contentDescription = null,
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                alpha = if (index <= value - 1) 1f else 0f
                            }
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val sliderPlaceable = measurables[0].measure(constraints)
        val starPlaceables = measurables
            .drop(1)
            .take(levels)
            .map { it.measure(constraints) }
        val starWidth = starPlaceables[0].width
        val startHeight = starPlaceables[0].height
        val offsetY = sliderPlaceable.height / 2 - startHeight / 2

        layout(sliderPlaceable.width, sliderPlaceable.height) {
            sliderPlaceable.placeRelative(0, 0)

            val offsetX = (sliderPlaceable.width - starWidth / 2 - thumbSize / 2) / (levels - 1)
            starPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x = -thumbSize / 2 + offsetX * index, y = offsetY)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun StarSliderPreview() = KaleyraTheme {
    StarSlider(
        value = 3f,
        onValueChange = {},
        levels = 5,
        modifier = Modifier.padding(10.dp)
    )
}