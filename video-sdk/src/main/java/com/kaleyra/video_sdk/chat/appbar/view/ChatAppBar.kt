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

package com.kaleyra.video_sdk.chat.appbar.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.common.button.BackIconButton
import com.kaleyra.video_sdk.common.button.IconButton
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.common.topappbar.TopAppBar

internal const val SubtitleTag = "SubtitleTag"
internal const val BouncingDotsTag = "BouncingDots"
internal const val ChatActionsTag = "ChatActionsTag"

@Composable
internal fun ChatAppBar(
    isInCall: Boolean = false,
    actions: ImmutableSet<ChatAction>,
    onBackPressed: () -> Unit = { },
    content: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        navigationIcon = { BackIconButton(onClick = onBackPressed) },
        content = content,
        actions = { if (!isInCall) Actions(actions = actions) }
    )
}

@Composable
internal fun Actions(actions: ImmutableSet<ChatAction>) {
    Row(Modifier.testTag(ChatActionsTag)) {
        actions.value.forEach {
            when (it) {
                is ChatAction.AudioCall -> {
                    IconButton(
                        icon = painterResource(R.drawable.ic_kaleyra_audio_call),
                        iconDescription = stringResource(id = R.string.kaleyra_start_audio_call),
                        onClick = it.onClick
                    )
                }

                is ChatAction.AudioUpgradableCall -> {
                    IconButton(
                        icon = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
                        iconDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call),
                        onClick = it.onClick
                    )
                }

                is ChatAction.VideoCall -> {
                    IconButton(
                        icon = painterResource(R.drawable.ic_kaleyra_video_call),
                        iconDescription = stringResource(id = R.string.kaleyra_start_video_call),
                        onClick = it.onClick
                    )
                }
            }
        }
    }
}
