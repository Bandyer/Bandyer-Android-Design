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

package com.kaleyra.video_sdk.call.stream.view.thumbnail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme

const val ThumbnailTag = "ThumbnailTag"
private val ThumbnailShape = RoundedCornerShape(16.dp)

@Composable
internal fun Thumbnail(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val size = min(configuration.screenHeightDp.dp, configuration.screenWidthDp.dp) / 4
    Box(
        modifier
            .size(size)
            .clip(ThumbnailShape)
            .background(colorResource(id = R.color.kaleyra_color_background_dark))
            .border(
                border = BorderStroke(1.dp, colorResource(id = R.color.kaleyra_color_background)),
                shape = ThumbnailShape
            )
            .testTag(ThumbnailTag),
        content = content
    )
}

@Preview
@Composable
internal fun ThumbnailPreview() {
    KaleyraTheme {
        Thumbnail(content = { })
    }
}
