package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object CallActionsMapper {

    // TODO change this when I know when to add the virtual background action
    fun Flow<CallUI>.toCallActions(): Flow<List<CallAction>> {
        return flatMapLatest { it.actions }
            .map { actions ->
                actions.mapNotNull {
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
                } + CallAction.VirtualBackground()
            }
    }
}