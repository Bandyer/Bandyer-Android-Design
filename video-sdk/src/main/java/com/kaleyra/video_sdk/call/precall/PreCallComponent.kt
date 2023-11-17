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

package com.kaleyra.video_sdk.call.precall

import android.net.Uri
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.featuredSettings
import com.kaleyra.video_sdk.call.precall.model.PreCallUiState
import com.kaleyra.video_sdk.call.callinfowidget.CallInfoWidget
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.core.StreamContainer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.usermessages.view.UserMessageSnackbarHandler
import com.kaleyra.video_sdk.R

@Composable
internal fun <T: PreCallUiState<T>> PreCallComponent(
    uiState: T,
    subtitle: String,
    userMessage: UserMessage? = null,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = modifier.fillMaxSize()) {
            StreamContainer {
                val isGroupCall = uiState.participants.count() > 1
                Stream(
                    streamView = uiState.video?.view?.featuredSettings() ?: ImmutableView(View(LocalContext.current)),
                    avatar = if (isGroupCall) ImmutableUri(Uri.EMPTY) else uiState.avatar,
                    avatarPlaceholder = if (isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    avatarError = if (isGroupCall) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar_bold,
                    avatarVisible = uiState.participants.value.isNotEmpty() && ((uiState.video == null && !uiState.isVideoIncoming) || (uiState.video != null && uiState.video?.view == null && uiState.video?.isEnabled == false) || uiState.video?.isEnabled == false),
                    showOverlay = true
                )
            }

            Column {
                CallInfoWidget(
                    onBackPressed = onBackPressed,
                    title = uiState.participants.value.joinToString(separator = ", "),
                    subtitle = if (!uiState.isLink && !uiState.isConnecting) subtitle else stringResource(id = R.string.kaleyra_call_status_connecting),
                    watermarkInfo = uiState.watermarkInfo,
                    recording = false,
                    modifier = Modifier.statusBarsPadding()
                )

                UserMessageSnackbarHandler(userMessage = userMessage)
            }

            content?.invoke(this)
        }
    }
}