package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.CallUI
import kotlinx.coroutines.flow.*

internal object CallActionsMapper {

    // TODO add test for this
    fun Flow<CallUI>.toMe(): Flow<CallParticipant.Me> = flatMapLatest { it.participants }.map { it.me }

    fun Flow<CallUI>.toCallActions(): Flow<List<CallAction>> {
        return combine(
            flatMapLatest { it.actions },
            flatMapLatest { it.state }
        ) { actions, callState ->
            val isConnected = callState is Call.State.Connected
            actions.mapNotNull { list ->
                when (list) {
                    is CallUI.Action.ToggleMicrophone -> CallAction.Microphone()
                    is CallUI.Action.ToggleCamera -> CallAction.Camera()
                    is CallUI.Action.SwitchCamera -> CallAction.SwitchCamera()
                    is CallUI.Action.HangUp -> CallAction.HangUp()
                    is CallUI.Action.Audio -> CallAction.Audio()
                    is CallUI.Action.OpenChat.Full -> CallAction.Chat()
                    is CallUI.Action.FileShare -> CallAction.FileShare(isEnabled = isConnected)
                    is CallUI.Action.ScreenShare -> CallAction.ScreenShare(isEnabled = isConnected)
                    is CallUI.Action.OpenWhiteboard.Full -> CallAction.Whiteboard(isEnabled = isConnected)
                    else -> null
                }
            }
        }
    }

    fun Flow<CallParticipant.Me>.isCameraEnabled(): Flow<Boolean> =
        this.flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.video.firstOrNull { it is Input.Video.Camera } != null
                }
            }
            .filterNotNull()
            .flatMapLatest { it.video }
            .filterNotNull()
            .flatMapLatest { it.enabled }

    fun Flow<CallParticipant.Me>.isMicEnabled(): Flow<Boolean> =
        this.flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.audio.firstOrNull { it != null } != null
                }
            }
            .filterNotNull()
            .flatMapLatest { it.audio }
            .filterNotNull()
            .flatMapLatest { it.enabled }
}