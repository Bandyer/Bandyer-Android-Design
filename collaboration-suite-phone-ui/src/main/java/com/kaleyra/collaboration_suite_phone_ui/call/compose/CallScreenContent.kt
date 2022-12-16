package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import android.net.Uri
import android.view.SurfaceView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.R
import kotlin.math.ceil

@Composable
internal fun StreamHandler() {

}

@Composable
internal fun Stream(isFirstStream: Boolean = false, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black,
        contentColor = Color.White
    ) {
        AndroidView(
            factory = { SurfaceView(it) },
            modifier = Modifier.fillMaxSize()
        )

        Row(Modifier.padding(horizontal = 8.dp)) {
//            if (isFirstStream) {
//                IconButton(
//                    icon = rememberVectorPainter(image = Icons.Filled.ArrowBack),
//                    iconDescription = stringResource(id = R.string.kaleyra_back),
//                    onClick = { }
//                )
//            }

            Text(
                text = "ste1",
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .weight(1f),
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_enter_fullscreen),
                iconDescription = "desd",
                onClick = { /*TODO*/ }
            )
        }

    }
}

@Stable
internal fun Modifier.pulse(durationMillis: Int = 1000): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        )
    )
    alpha(alpha)
}

@Composable
internal fun Avatar(uri: Uri?) {
    val placeholderFilter = ColorFilter.tint(color = MaterialTheme.colors.onPrimary)
    var colorFilter by remember { mutableStateOf<ColorFilter?>(placeholderFilter) }
    AsyncImage(
        model = uri,
        contentDescription = stringResource(id = R.string.kaleyra_chat_avatar_desc),
        modifier = Modifier
            .clip(CircleShape)
            .background(color = colorResource(R.color.kaleyra_color_grey_light))
            .size(40.dp),
        placeholder = painterResource(R.drawable.ic_kaleyra_avatar),
        error = painterResource(R.drawable.ic_kaleyra_avatar),
        contentScale = ContentScale.Crop,
        onSuccess = { colorFilter = null },
        colorFilter = colorFilter
    )
}

@Composable
internal fun CallInfoWidget() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            icon = rememberVectorPainter(image = Icons.Filled.ArrowBack),
            iconDescription = stringResource(id = R.string.kaleyra_back),
            onClick = { }
        )

        // TODO add watermark

        Spacer(modifier = Modifier.weight(1f))

        RecordingLabel()
    }
}


@Composable
internal fun RecordingLabel(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.kaleyra_recording_background_color),
                shape = RoundedCornerShape(5.dp)
            )
            .padding(top = 4.dp, bottom = 4.dp, start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_kaleyra_recording_dot),
            contentDescription = null,
            tint = colorResource(id = R.color.kaleyra_recording_color),
            modifier = Modifier
                .size(20.dp)
                .pulse()
        )
        Text(
            text = stringResource(id = R.string.kaleyra_call_info_rec).toUpperCase(Locale.current),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
internal fun CallScreenContent(
    streams: List<Int> = listOf(1, 1),
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        val columns = when {
            isPortrait && maxWidth < 600.dp -> 1
            isPortrait && streams.size > 2 -> 2
            streams.size > 1 -> 2
            else -> 1
        }

        Grid(columns = columns) {
            (1..streams.size).forEachIndexed { index, _ ->
                Stream(
                    isFirstStream = index == 0,
                    modifier = Modifier.padding(top = if (index < columns) 40.dp else 0.dp)
                )
            }
        }

        CallInfoWidget()
    }
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    columns: Int,
    children: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = children
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth) {
            "Unbounded width not supported"
        }

        val rows = ceil(measurables.size / columns.toFloat()).toInt()

        val itemWidth = constraints.maxWidth / columns
        val itemHeight = constraints.maxHeight / rows
        val itemConstraints = constraints.copy(maxWidth = itemWidth, maxHeight = itemHeight)

        val placeables = measurables.map { measurable ->
            measurable.measure(itemConstraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var xPosition = 0
            var yPosition = 0

            val nOfLastRowItems = measurables.size - (columns * (rows - 1))
            var currentRow = 0
            placeables.forEachIndexed { index, placeable ->
                placeable.place(x = xPosition, y = yPosition)

                if (index % columns == columns - 1) {
                    yPosition += itemHeight
                    xPosition = 0
                    currentRow++
                } else {
                    xPosition += itemWidth
                }

                if (currentRow == rows - 1 && xPosition == 0) {
                    xPosition += (constraints.maxWidth - (nOfLastRowItems * itemWidth)) / 2
                }
            }
        }
    }
}

@Preview
@Composable
fun AvatarPreview() {
    KaleyraTheme {
        Avatar(null)
    }
}

@Preview
@Composable
fun CallInfoWidgetPreview() {
    KaleyraTheme {
        CallInfoWidget()
    }
}

@Preview
@Composable
fun RecordingLabelPreview() {
    KaleyraTheme {
        RecordingLabel()
    }
}

@Preview
@Composable
fun StreamPreview() {
    KaleyraTheme {
        Stream()
    }
}

//@Preview(name = "Light Mode")
//@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview
@Composable
fun CallScreenContentPreview() {
    KaleyraTheme {
        CallScreenContent()
    }
}