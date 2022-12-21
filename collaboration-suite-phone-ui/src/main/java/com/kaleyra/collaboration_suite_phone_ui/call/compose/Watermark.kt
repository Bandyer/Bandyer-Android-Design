package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

private val MaxWatermarkHeight = 80.dp
private val MaxWatermarkWidth = 300.dp

@Composable
internal fun Watermark(image: Painter, text: String? = null) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val logoRatio = with(image.intrinsicSize) { width / height }
        Image(
            painter = image,
            contentDescription = stringResource(id = R.string.kaleyra_company_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .heightIn(max = MaxWatermarkHeight)
                .widthIn(max = MaxWatermarkWidth)
                .aspectRatio(logoRatio)
        )
        if (text != null) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun CallInfoWidgetPreview() {
    KaleyraTheme {
        Watermark(
            image = painterResource(id = R.drawable.kaleyra_z_screen_share),
            text = "logo text"
        )
    }
}