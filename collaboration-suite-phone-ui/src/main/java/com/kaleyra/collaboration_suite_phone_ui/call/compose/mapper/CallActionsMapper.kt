package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasAudio
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasVideo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.hasVirtualBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

internal object CallActionsMapper {

    fun Flow<CallUI>.toCallActions(): Flow<List<CallAction>> {
        return combine(
            flatMapLatest { it.actions },
            hasVirtualBackground(),
            hasVideo(),
            hasAudio()
        ) { actions, hasVirtualBackground, hasVideo, hasAudio ->
            val result = mutableListOf<CallAction>()

            val hasMicrophone = actions.any { action -> action is CallUI.Action.ToggleMicrophone && hasAudio }
            val hasCamera = actions.any { action -> action is CallUI.Action.ToggleCamera && hasVideo }
            val switchCamera = actions.any { action -> action is CallUI.Action.SwitchCamera && hasVideo }
            val hangUp = actions.any { action -> action is CallUI.Action.HangUp }
            val audio = actions.any { action -> action is CallUI.Action.Audio }
            val chat = actions.any { action -> action is CallUI.Action.OpenChat.Full }
            val fileShare = actions.any { action -> action is CallUI.Action.FileShare }
            val screenShare = actions.any { action -> action is CallUI.Action.ScreenShare }
            val whiteboard = actions.any { action -> action is CallUI.Action.OpenWhiteboard.Full }

            if (hasMicrophone) result += CallAction.Microphone()
            if (hasCamera) result += CallAction.Camera()
            if (switchCamera) result += CallAction.SwitchCamera()
            if (audio) result += CallAction.Audio()
            if (chat) result += CallAction.Chat()
            if (fileShare) result += CallAction.FileShare()
            if (screenShare) result += CallAction.ScreenShare()
            if (whiteboard) result += CallAction.Whiteboard()
            if (hasVirtualBackground) result += CallAction.VirtualBackground()

            if (hangUp) {
                if (result.size >= 4) result.add(3, CallAction.HangUp())
                else result.add(CallAction.HangUp())
            }

            result
        }

    }
}