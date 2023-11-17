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

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasAudio
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.hasVirtualBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

internal object CallActionsMapper {

    fun Flow<CallUI>.toCallActions(companyId: Flow<String>): Flow<List<CallAction>> =
        combine(
            flatMapLatest { it.actions },
            hasVirtualBackground(),
            isAudioOnly(),
            hasAudio(),
            isGroupCall(companyId)
        ) { actions, hasVirtualBackground, isAudioOnly, hasAudio, isGroupCall ->
            val result = mutableListOf<CallAction>()

            val hasMicrophone = actions.any { action -> action is CallUI.Action.ToggleMicrophone && hasAudio }
            val hasCamera = actions.any { action -> action is CallUI.Action.ToggleCamera && !isAudioOnly }
            val switchCamera = actions.any { action -> action is CallUI.Action.SwitchCamera && !isAudioOnly }
            val hangUp = actions.any { action -> action is CallUI.Action.HangUp }
            val audio = actions.any { action -> action is CallUI.Action.Audio }
            val chat = actions.any { action -> action is CallUI.Action.OpenChat.Full && !isGroupCall }
            val fileShare = actions.any { action -> action is CallUI.Action.FileShare }
            val screenShare = actions.any { action -> action is CallUI.Action.ScreenShare }
            val whiteboard = actions.any { action -> action is CallUI.Action.OpenWhiteboard.Full }

            if (hasMicrophone) result += CallAction.Microphone()
            if (hasCamera) result += CallAction.Camera()
            if (switchCamera) result += CallAction.SwitchCamera()
            if (chat) result += CallAction.Chat()
            if (whiteboard) result += CallAction.Whiteboard()
            if (audio) result += CallAction.Audio()
            if (fileShare) result += CallAction.FileShare()
            if (screenShare) result += CallAction.ScreenShare()
            if (hasVirtualBackground) result += CallAction.VirtualBackground()

            if (hangUp) {
                if (result.size >= 4) result.add(3, CallAction.HangUp())
                else result.add(CallAction.HangUp())
            }

            result
        }.distinctUntilChanged()
}