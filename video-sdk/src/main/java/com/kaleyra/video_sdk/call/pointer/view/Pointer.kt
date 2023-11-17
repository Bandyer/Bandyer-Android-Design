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

package com.kaleyra.video_sdk.call.pointer.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.countdowntimer.rememberCountdownTimerState
import com.kaleyra.video_sdk.extensions.TextStyleExtensions.shadow

val PointerSize = 16.dp
const val PointerAutoHideMs = 3000L

@Composable
internal fun TextPointer(
    username: String,
    onTextWidth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = LocalTextStyle.current
    val countDown by rememberCountdownTimerState(initialMillis = PointerAutoHideMs)
    val textAlpha by animateFloatAsState(targetValue =  if (countDown > 0L) 1f else 0f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Pointer()
        Text(
            text = username,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .onGloballyPositioned { onTextWidth(it.size.width) }
                .graphicsLayer { alpha = textAlpha },
            style = textStyle.shadow()
        )
    }
}

@Composable
internal fun Pointer(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .size(PointerSize)
            .background(
                color = MaterialTheme.colors.secondary,
                shape = CircleShape
            )
    )
}