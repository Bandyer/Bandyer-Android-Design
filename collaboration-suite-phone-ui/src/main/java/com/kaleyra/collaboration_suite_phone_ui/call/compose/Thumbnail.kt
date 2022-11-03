package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.swipeable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_core_ui.R
import kotlin.math.roundToInt
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take

private val ThumbnailShape = RoundedCornerShape(16.dp)


internal typealias Coordinates = Pair<Float, Float>

internal class AnchorableState<T>(
    initialValue: T
) {
    var currentValue: T by mutableStateOf(initialValue)
        private set

    val offset: State<Coordinates> get() = offsetState

    private val offsetState = mutableStateOf(Pair(0f, 0f))

    internal var anchors by mutableStateOf(emptyMap<Coordinates, T>())

    private val latestNonEmptyAnchorsFlow: Flow<Map<Coordinates, T>> =
        snapshotFlow { anchors }
            .filter { it.isNotEmpty() }
            .take(1)
}

internal fun <T> Modifier.anchorable(
    state: AnchorableState<T>,
    anchors: Map<Coordinates, T>
) = composed {
    require(anchors.isNotEmpty()) {
        "You must have at least one anchor."
    }
    require(anchors.values.distinct().count() == anchors.size) {
        "You cannot have two anchors mapped to the same state."
    }
    LaunchedEffect(anchors, state) {
        state.anchors = anchors
    }

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Modifier
        .offset {
            IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {

                }
            ) { change, dragAmount ->
                change.consume()
                val newY = offsetY + dragAmount.y
                if (newY < 0) {
                    offsetY += dragAmount.y
                }
                offsetX += dragAmount.x
                Log.e("Thumbnail", "x: $offsetX, y: $offsetY")
            }
        }
}

private fun <T> Map<Coordinates, T>.getCoordinates(state: T): Coordinates? {
    return entries.firstOrNull { it.value == state }?.key
}

@Composable
internal fun DraggableThumbnail(content: () -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Thumbnail(
        modifier = Modifier
            .offset {
                IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val res1 = (offsetX - 0f) * (offsetX - 0f) + (offsetY - 0f) * (offsetY - 0f)
                        val res2 = (offsetX + 777f) * (offsetX + 777f) + (offsetY + 35f) * (offsetY + 35f)
                        if (res1 > res2) {
                            offsetX = -777f
                            offsetY = -35f
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newY = offsetY + dragAmount.y
                    if (newY < 0) {
                        offsetY += dragAmount.y
                    }
                    offsetX += dragAmount.x
                    Log.e("Thumbnail", "x: $offsetX, y: $offsetY")
                }
            }

    ) {

    }
}


@Composable
internal fun Thumbnail(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val size = min(configuration.screenHeightDp.dp, configuration.screenWidthDp.dp) / 4
    Box(
        modifier
            .size(size)
            .background(
                color = colorResource(id = R.color.kaleyra_color_background_dark),
                shape = ThumbnailShape
            )
            .border(
                border = BorderStroke(1.dp, colorResource(id = R.color.kaleyra_color_background)),
                shape = ThumbnailShape
            )
    ) {
//        AsyncImage(
//            model = avatar,
//            contentDescription = stringResource(id = com.kaleyra.collaboration_suite_phone_ui.R.string.kaleyra_chat_avatar_desc),
//            modifier = Modifier
//                .clip(CircleShape)
//                .background(color = colorResource(com.kaleyra.collaboration_suite_phone_ui.R.color.kaleyra_color_grey_light))
//                .size(40.dp),
//            placeholder = painterResource(com.kaleyra.collaboration_suite_phone_ui.R.drawable.ic_kaleyra_avatar),
//            error = painterResource(com.kaleyra.collaboration_suite_phone_ui.R.drawable.ic_kaleyra_avatar),
//            contentScale = ContentScale.Crop,
//            onSuccess = { colorFilter = null },
//            colorFilter = colorFilter
//        )
        content.invoke()
    }
}

@Preview
@Composable
internal fun ThumbnailPreview() {
    KaleyraTheme {
        Thumbnail {}
    }
}