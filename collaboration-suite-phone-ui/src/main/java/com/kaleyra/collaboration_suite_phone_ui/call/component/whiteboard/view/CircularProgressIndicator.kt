package com.kaleyra.collaboration_suite_phone_ui.call.component.whiteboard.view

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

const val CircularProgressIndicatorTag = "CircularProgressIndicatorTag"

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
    androidx.compose.material.CircularProgressIndicator(
        progress = progress,
        color = color,
        strokeWidth = strokeWidth,
        modifier = Modifier
            .size(size)
            .testTag(CircularProgressIndicatorTag)
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

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CircularProgressIndicatorPreview() {
    KaleyraTheme {
        Surface {
            CircularProgressIndicator(
                progress = .6f,
                color = MaterialTheme.colors.secondaryVariant,
                size = 56.dp,
                strokeWidth = ProgressIndicatorDefaults.StrokeWidth
            )
        }
    }
}