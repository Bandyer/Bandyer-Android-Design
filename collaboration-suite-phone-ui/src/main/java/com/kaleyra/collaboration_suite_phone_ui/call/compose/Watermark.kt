package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

private val MaxWatermarkHeight = 80.dp
private val MaxWatermarkWidth = 300.dp

@Composable
internal fun Watermark(watermarkInfo: WatermarkInfo, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (watermarkInfo.image != null) {
            val painter = painterResource(id = watermarkInfo.image)
            val logoRatio = with(painter.intrinsicSize) { width / height }
            Image(
                painter = painter,
                contentDescription = stringResource(id = R.string.kaleyra_company_logo),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .heightIn(max = MaxWatermarkHeight)
                    .widthIn(max = MaxWatermarkWidth)
                    .aspectRatio(logoRatio)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        if (watermarkInfo.text != null) {
            Text(
                text = watermarkInfo.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
fun CallInfoWidgetPreview() {
    KaleyraTheme {
        Watermark(
            watermarkInfo = WatermarkInfo(R.drawable.kaleyra_z_screen_share, "text")
        )
    }
}