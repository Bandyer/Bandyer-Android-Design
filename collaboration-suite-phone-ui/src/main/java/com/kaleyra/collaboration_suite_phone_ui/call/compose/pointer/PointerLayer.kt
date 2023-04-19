package com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import java.lang.Integer.max

const val PointerLayerTag = "PointerLayerTag"

@Composable
internal fun BoxScope.PointerLayer(pointer: PointerUi) {
    val density = LocalDensity.current

    val pointerSize = remember { with(density) { PointerSize.toPx() } / 2 }
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    var textWidth by remember { mutableStateOf(0) }

    val offsetX by animateFloatAsState(targetValue = (pointer.x / 100) * size.width - max(textWidth, pointerSize.toInt()))
    val offsetY by animateFloatAsState(targetValue = (pointer.y / 100) * size.height - pointerSize)

    Box(
        modifier = Modifier
            .matchParentSize()
            .onGloballyPositioned { size = it.size }
            .testTag(PointerLayerTag)
    ) {
        TextPointer(
            username = pointer.username,
            onTextWidth = { textWidth = it / 2 },
            modifier = Modifier.offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
        )
    }
}