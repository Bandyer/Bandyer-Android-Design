package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.kaleyra.collaboration_suite_phone_ui.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlin.math.roundToInt

@Composable
internal fun Whiteboard(
    uploading: Boolean,
    uploadProgress: Float,
    error: Boolean,
    onCloseClick: () -> Unit
) {
    SubMenuLayout(
        title = stringResource(id = R.string.kaleyra_whiteboard),
        onCloseClick = onCloseClick,
        modifier = Modifier.background(color = colorResource(id = R.color.kaleyra_color_loading_whiteboard_background))
    ) {

    }
}

@Composable
internal fun UploadCard(progress: Float, error: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (error) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_close),
                        contentDescription = null,
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colors.secondaryVariant,
                        size = 64.dp,
                        strokeWidth = ProgressIndicatorDefaults.StrokeWidth
                    )
                    Text(
                        text = stringResource(
                            id = R.string.kaleyra_file_upload_percentage,
                            (progress * 100).roundToInt()
                        ), fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(id = if (error) R.string.kaleyra_whiteboard_error_title else R.string.kaleyra_whiteboard_uploading_file),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = if (error) R.string.kaleyra_whiteboard_error_subtitle else R.string.kaleyra_whiteboard_compressing),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
internal fun CircularProgressIndicator(progress: Float, color: Color, size: Dp, strokeWidth: Dp) {
    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
    }
    Canvas(Modifier.size(size)) {
        drawCircularBackground(
            color = color.copy(alpha = ProgressIndicatorDefaults.IndicatorBackgroundOpacity),
            stroke = stroke
        )
    }
    CircularProgressIndicator(
        progress = progress,
        color = color,
        strokeWidth = strokeWidth,
        modifier = Modifier.size(size)
    )
}

private fun DrawScope.drawCircularBackground(
    color: Color, stroke: Stroke
) {
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(diameterOffset, diameterOffset),
        size = Size(arcDimen, arcDimen),
        style = stroke
    )
}

@Composable
internal fun LoadingError(loading: Boolean, onReloadClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing)
            )
        )
        IconButton(onClick = onReloadClick) {
            Icon(painter = painterResource(id = R.drawable.ic_kaleyra_reload),
                contentDescription = stringResource(id = R.string.kaleyra_error_button_reload),
                tint = Color.Black,
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        rotationZ = if (loading) rotation else 0f
                    })
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.kaleyra_error_title), color = Color.Black
        )
        Text(
            text = stringResource(id = R.string.kaleyra_error_subtitle),
            color = Color.DarkGray,
            fontSize = 12.sp
        )
    }
}

@Preview
@Composable
internal fun UploadingWindowPreview() {
    KaleyraTheme {
        UploadCard(.8f, error = false)
    }
}

@Preview
@Composable
internal fun UploadingWindowPreview2() {
    KaleyraTheme {
        UploadCard(.8f, error = true)
    }
}

@Preview
@Composable
internal fun LoadingErrorPreview() {
    KaleyraTheme {
        LoadingError(loading = false, onReloadClick = {})
    }
}