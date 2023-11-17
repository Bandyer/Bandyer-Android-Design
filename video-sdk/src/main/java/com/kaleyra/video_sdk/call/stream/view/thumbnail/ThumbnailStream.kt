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

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.thumbnailSettings
import com.kaleyra.video_sdk.call.pointer.view.PointerStreamWrapper
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.core.StreamContainer
import com.kaleyra.video_sdk.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ThumbnailStream(
    stream: StreamUi,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false
) {
    Thumbnail(
        modifier = modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClickLabel = stringResource(id = R.string.kaleyra_move_to_featured_streams),
            role = Role.Button,
            onClick = onClick,
            onDoubleClick = onDoubleClick
        )
    ) {
        StreamContainer(
            backgroundColor = Color.DarkGray,
            contentColor = Color.White
        ) {
            PointerStreamWrapper(
                streamView = stream.video?.view,
                pointerList = stream.video?.pointers,
                isTesting = isTesting
            ) { hasPointers ->
                val shouldFit = stream.video?.isScreenShare == true || hasPointers
                Stream(
                    streamView = stream.video?.view?.thumbnailSettings(
                        scaleType = if (shouldFit) StreamView.ScaleType.Fit else StreamView.ScaleType.Fill(1f)
                    ) ?: ImmutableView(View(LocalContext.current)),
                    avatar = stream.avatar,
                    avatarVisible = stream.video?.view == null || !stream.video.isEnabled,
                    avatarSize = 64.dp
                )
            }
        }
    }
}
