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

package com.kaleyra.video_sdk.common.avatar.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

@Composable
internal fun Avatar(
    uri: ImmutableUri?,
    contentDescription: String,
    @DrawableRes placeholder: Int,
    @DrawableRes error: Int,
    contentColor: Color,
    backgroundColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    var isImageLoaded by remember { mutableStateOf(false) }
    val placeholderFilter by rememberUpdatedState(newValue = ColorFilter.tint(color = contentColor))
    val colorFilter by remember {
        derivedStateOf {
            if (isImageLoaded) null else placeholderFilter
        }
    }
    AsyncImage(
        model = uri?.value,
        contentDescription = contentDescription,
        modifier = modifier
            .clip(CircleShape)
            .background(color = backgroundColor)
            .size(size),
        placeholder = painterResource(id = placeholder),
        error = painterResource(id = error),
        contentScale = ContentScale.Crop,
        onSuccess = { isImageLoaded = true },
        colorFilter = colorFilter
    )
}