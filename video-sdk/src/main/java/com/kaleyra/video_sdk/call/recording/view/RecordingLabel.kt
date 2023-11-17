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

package com.kaleyra.video_sdk.call.recording.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.extensions.ModifierExtensions.pulse

internal const val RecordingDotTestTag = "RecordingDotTestTag"

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
        RecordingDot()
        Text(
            text = stringResource(id = R.string.kaleyra_call_info_rec).uppercase(),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
internal fun RecordingDot(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_kaleyra_recording_dot),
        contentDescription = null,
        tint = colorResource(id = R.color.kaleyra_recording_color),
        modifier = modifier
            .size(20.dp)
            .pulse()
            .testTag(RecordingDotTestTag)
    )
}

@Preview
@Composable
internal fun RecordingLabelPreview() {
    KaleyraTheme {
        RecordingLabel()
    }
}