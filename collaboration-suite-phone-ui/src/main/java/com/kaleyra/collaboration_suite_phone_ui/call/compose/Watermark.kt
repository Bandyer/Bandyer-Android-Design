package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

private val MaxWatermarkHeight = 80.dp
private val MaxWatermarkWidth = 300.dp

const val WatermarkTag = "WatermarkTag"

@Composable
internal fun Watermark(watermarkInfo: WatermarkInfo, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.testTag(WatermarkTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            watermarkInfo.logo != null && (watermarkInfo.logo.dark != Uri.EMPTY || watermarkInfo.logo.light != Uri.EMPTY) -> {
                AsyncImage(
                    model = watermarkInfo.logo.let { if (!isSystemInDarkTheme()) it.light else it.dark },
                    contentDescription = stringResource(id = R.string.kaleyra_company_logo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .heightIn(max = MaxWatermarkHeight)
                        .widthIn(max = MaxWatermarkWidth),
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            watermarkInfo.text != null -> {
                Text(
                    text = watermarkInfo.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview
@Composable
fun CallInfoWidgetPreview() {
    KaleyraTheme {
        Watermark(
            watermarkInfo = WatermarkInfo(text = "text", logo = null)
        )
    }
}