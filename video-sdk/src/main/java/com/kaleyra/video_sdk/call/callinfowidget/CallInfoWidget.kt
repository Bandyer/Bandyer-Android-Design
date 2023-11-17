/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.callinfowidget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.kaleyra.video_sdk.common.button.BackIconButton
import com.kaleyra.video_sdk.common.text.Ellipsize
import com.kaleyra.video_sdk.common.text.EllipsizeText
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.call.callinfowidget.view.Watermark
import com.kaleyra.video_sdk.call.recording.view.RecordingLabel
import com.kaleyra.video_sdk.extensions.TextStyleExtensions.shadow
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.extensions.ModifierExtensions.horizontalCutoutPadding
import com.kaleyra.video_sdk.extensions.ModifierExtensions.verticalGradientScrim

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
            .horizontalCutoutPadding()
            .testTag(CallInfoWidgetTag)
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
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
            fontWeight = FontWeight.SemiBold,
            ellipsize = Ellipsize.Marquee,
            shadow = textStyle.shadow
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
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
            watermarkInfo = WatermarkInfo(logo = null, text = "text"),
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