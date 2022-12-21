package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.BackIconButton
import com.kaleyra.collaboration_suite_phone_ui.call.compose.EllipsizeText
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Watermark
import com.kaleyra.collaboration_suite_phone_ui.call.shadow
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.verticalGradientScrim

@Composable
internal fun CallInfoWidget(
    onBackPressed: () -> Unit,
    callInfo: CallInfoUi,
    header: Boolean = true,
    watermark: Boolean = true,
    recording: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalGradientScrim(
                color = Color.Black.copy(alpha = .5f),
                startYPercentage = 1f,
                endYPercentage = 0f
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = if (watermark) Alignment.Top else Alignment.CenterVertically
        ) {
            BackIconButton(onClick = onBackPressed)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                when {
                    watermark -> Watermark(
                        image = callInfo.watermarkImage,
                        text = callInfo.watermarkText
                    )
                    header -> Header(
                        title = callInfo.headerTitle,
                        subtitle = callInfo.headerSubtitle
                    )
                    else -> Unit
                }
            }

            if (recording) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.Center,
                    content = { RecordingLabel() }
                )
            }
        }
        if (watermark && header) {
            Header(
                title = callInfo.headerTitle,
                subtitle = callInfo.headerSubtitle,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
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
            callInfo = callInfoMock.copy(
                watermarkImage = painterResource(id = R.drawable.ic_100tb)
            ),
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
            callInfo = callInfoMock,
            watermark = false,
            recording = true
        )
    }
}