package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.hasVirtualBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

internal object CallActionsMapper {

    fun Flow<CallUI>.toCallActions(): Flow<List<CallAction>> {
        return combine(flatMapLatest { it.actions }, hasVirtualBackground()) {  actions, hasVirtualBackground ->
            val result = actions.mapNotNull {
                when (it) {
                    is CallUI.Action.ToggleMicrophone -> CallAction.Microphone()
                    is CallUI.Action.ToggleCamera -> CallAction.Camera()
                    is CallUI.Action.SwitchCamera -> CallAction.SwitchCamera()
                    is CallUI.Action.HangUp -> CallAction.HangUp()
                    is CallUI.Action.Audio -> CallAction.Audio()
                    is CallUI.Action.OpenChat.Full -> CallAction.Chat()
                    is CallUI.Action.FileShare -> CallAction.FileShare()
                    is CallUI.Action.ScreenShare -> CallAction.ScreenShare()
                    is CallUI.Action.OpenWhiteboard.Full -> CallAction.Whiteboard()
                    else -> null
                }
            }
            if (hasVirtualBackground) result + CallAction.VirtualBackground()
            else result
        }

    }
}