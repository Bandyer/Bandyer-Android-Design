package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.BackIconButton
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Ellipsize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.EllipsizeText
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Watermark
import com.kaleyra.collaboration_suite_phone_ui.call.shadow
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.verticalGradientScrim

const val CallInfoWidgetTag = "CallInfoWidgetTag"

// NB: The title is actually an AndroidView, because there is not text ellipsize in compose
@Composable
internal fun CallInfoWidget(
    title: String,
    subtitle: String?,
    watermarkInfo: WatermarkInfo?,
    recording: Boolean,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .verticalGradientScrim(
                color = Color.Black.copy(alpha = .5f),
                startYPercentage = 1f,
                endYPercentage = 0f
            )
            .testTag(CallInfoWidgetTag)
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = if (watermarkInfo != null) Alignment.Top else Alignment.CenterVertically
        ) {
            BackIconButton(onClick = onBackPressed)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                if (watermarkInfo != null) {
                    Watermark(watermarkInfo = watermarkInfo)
                } else {
                    Header(title = title, subtitle = subtitle)
                }
            }

            if (recording) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center,
                    content = { RecordingLabel() }
                )
            }
        }
        if (watermarkInfo != null) {
            Header(title = title, subtitle = subtitle, modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}

@Composable
private fun Header(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.height(56.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val textStyle = LocalTextStyle.current.shadow()
        EllipsizeText(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            ellipsize = Ellipsize.Marquee,
            shadow = textStyle.shadow
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = Color.White,
                fontSize = 12.sp,
                style = textStyle
            )
        }
    }
}

@Preview
@Composable
internal fun CallInfoWidgetWithWatermarkPreview() {
    KaleyraTheme {
        CallInfoWidget(
            onBackPressed = { },
            title = "Title",
            subtitle = "Subtitle",
            watermarkInfo = WatermarkInfo(image = R.drawable.ic_kaleyra_screen_share),
            recording = true
        )
    }
}

@Preview
@Composable
internal fun CallInfoWidgetNoWatermarkPreview() {
    KaleyraTheme {
        CallInfoWidget(
            onBackPressed = { },
            title = "Title",
            subtitle = "Subtitle",
            watermarkInfo = null,
            recording = true
        )
    }
}