package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_core_ui.R

const val ThumbnailTag = "ThumbnailTag"
private val ThumbnailShape = RoundedCornerShape(16.dp)

@Composable
internal fun Thumbnail(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val size = min(configuration.screenHeightDp.dp, configuration.screenWidthDp.dp) / 4
    Box(
        modifier
            .size(size)
            .clip(ThumbnailShape)
            .background(colorResource(id = R.color.kaleyra_color_background_dark))
            .border(
                border = BorderStroke(1.dp, colorResource(id = R.color.kaleyra_color_background)),
                shape = ThumbnailShape
            )
            .testTag(ThumbnailTag),
        content = content
    )
}

@Preview
@Composable
internal fun ThumbnailPreview() {
    KaleyraTheme {
        Thumbnail(content = { })
    }
}

//
//internal typealias Coordinates = Pair<Float, Float>
//
//internal class AnchorableState<T>(
//    initialValue: T
//) {
//    var currentValue: T by mutableStateOf(initialValue)
//        private set
//
//    val offset: State<Coordinates> get() = offsetState
//
//    private val offsetState = mutableStateOf(Pair(0f, 0f))
//
//    internal var anchors by mutableStateOf(emptyMap<Coordinates, T>())
//
//    private val latestNonEmptyAnchorsFlow: Flow<Map<Coordinates, T>> =
//        snapshotFlow { anchors }
//            .filter { it.isNotEmpty() }
//            .take(1)
//}
//
//internal fun <T> Modifier.anchorable(
//    state: AnchorableState<T>,
//    anchors: Map<Coordinates, T>
//) = composed {
//    require(anchors.isNotEmpty()) {
//        "You must have at least one anchor."
//    }
//    require(anchors.values.distinct().count() == anchors.size) {
//        "You cannot have two anchors mapped to the same state."
//    }
//    LaunchedEffect(anchors, state) {
//        state.anchors = anchors
//    }
//
//    var offsetX by remember { mutableStateOf(0f) }
//    var offsetY by remember { mutableStateOf(0f) }
//    Modifier
//        .offset {
//            IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
//        }
//        .pointerInput(Unit) {
//            detectDragGestures(
//                onDragEnd = {
//
//                }
//            ) { change, dragAmount ->
//                change.consume()
//                val newY = offsetY + dragAmount.y
//                if (newY < 0) {
//                    offsetY += dragAmount.y
//                }
//                offsetX += dragAmount.x
//                Log.e("Thumbnail", "x: $offsetX, y: $offsetY")
//            }
//        }
//}
//
//private fun <T> Map<Coordinates, T>.getCoordinates(state: T): Coordinates? {
//    return entries.firstOrNull { it.value == state }?.key
//}
//
//@Composable
//internal fun DraggableThumbnail(content: () -> Unit) {
//    var offsetX by remember { mutableStateOf(0f) }
//    var offsetY by remember { mutableStateOf(0f) }
//
//    Thumbnail(
//        modifier = Modifier
//            .offset {
//                IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
//            }
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDragEnd = {
//                        val res1 = (offsetX - 0f) * (offsetX - 0f) + (offsetY - 0f) * (offsetY - 0f)
//                        val res2 = (offsetX + 777f) * (offsetX + 777f) + (offsetY + 35f) * (offsetY + 35f)
//                        if (res1 > res2) {
//                            offsetX = -777f
//                            offsetY = -35f
//                        } else {
//                            offsetX = 0f
//                            offsetY = 0f
//                        }
//                    }
//                ) { change, dragAmount ->
//                    change.consume()
//                    val newY = offsetY + dragAmount.y
//                    if (newY < 0) {
//                        offsetY += dragAmount.y
//                    }
//                    offsetX += dragAmount.x
//                    Log.e("Thumbnail", "x: $offsetX, y: $offsetY")
//                }
//            }
//
//    ) {
//
//    }
//}
