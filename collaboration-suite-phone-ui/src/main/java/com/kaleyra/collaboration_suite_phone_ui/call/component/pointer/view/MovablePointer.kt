package com.kaleyra.collaboration_suite_phone_ui.call.component.pointer.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.kaleyra.collaboration_suite_phone_ui.call.component.pointer.model.PointerUi
import java.lang.Integer.max

const val MovablePointerTag = "MovablePointerTag"

@Composable
internal fun MovablePointer(pointer: PointerUi, parentSize: IntSize, scale: FloatArray) {
    val density = LocalDensity.current

    val pointerSize = remember { with(density) { PointerSize.toPx() } }
    var textWidth by remember { mutableStateOf(0) }

    val centerOffsetX by remember { derivedStateOf { max(textWidth, pointerSize.toInt() / 2) } }
    val centerOffsetY = remember { pointerSize / 2 }

    val offsetX by animateFloatAsState(targetValue = (pointer.x / 100) * parentSize.width - centerOffsetX)
    val offsetY by animateFloatAsState(targetValue = (pointer.y / 100) * parentSize.height - centerOffsetY)

    TextPointer(
        username = pointer.username,
        onTextWidth = { textWidth = it / 2 },
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .graphicsLayer {
                transformOrigin = TransformOrigin(.5f, .2f)
                scaleX /= scale[0]
                scaleY /= scale[1]
            }
            .testTag(MovablePointerTag)
    )
}