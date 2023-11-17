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

package com.kaleyra.video_sdk.chat.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction

internal object ChatActionsMapper {
    fun Set<ChatUI.Action>.mapToChatActions(call: (Call.PreferredType) -> Unit): Set<ChatAction> {
        return mutableSetOf<ChatAction>().apply {
            val actions = this@mapToChatActions.filterIsInstance<ChatUI.Action.CreateCall>()
            actions.firstOrNull { !it.preferredType.hasVideo() }?.also { action ->
                add(ChatAction.AudioCall { call(action.preferredType) })
            }
            actions.firstOrNull { !it.preferredType.isVideoEnabled() }?.also { action ->
                add(ChatAction.AudioUpgradableCall { call(action.preferredType) })
            }
            actions.firstOrNull { it.preferredType.isVideoEnabled() }?.also { action ->
                add(ChatAction.VideoCall { call(action.preferredType) })
            }
        }
    }
}