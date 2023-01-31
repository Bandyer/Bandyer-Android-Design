package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputMapper.mapToAudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputMapper.mapToBluetoothDeviceState
import kotlinx.coroutines.flow.*

internal object CallActionsMapper {

    fun Flow<CallUI>.isConnected(): Flow<Boolean> =
        flatMapLatest { it.state }.map { it is Call.State.Connected }

    fun Flow<CallUI>.toCurrentAudioDeviceUi(): Flow<AudioDeviceUi?> =
        flatMapLatest { it.currentAudioOutputDevice }.map { it?.mapToAudioDeviceUi() }

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
                }
            }
    }

    fun Flow<CallUI>.isMyCameraEnabled(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.video.firstOrNull { it is Input.Video.Camera } != null
                }
            }
            .filterNotNull()
            .flatMapLatest { it.video }
            .filterNotNull()
            .flatMapLatest { it.enabled }

    fun Flow<CallUI>.isMyMicEnabled(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.audio.firstOrNull { it != null } != null
                }
            }
            .filterNotNull()
            .flatMapLatest { it.audio }
            .filterNotNull()
            .flatMapLatest { it.enabled }

    private fun Flow<CallUI>.toMe(): Flow<CallParticipant.Me> =
        flatMapLatest { it.participants }.map { it.me }

    fun AudioOutputDevice.mapToAudioDeviceUi(): AudioDeviceUi =
        when (this) {
            is AudioOutputDevice.NONE -> AudioDeviceUi.Muted
            is AudioOutputDevice.EARPIECE -> AudioDeviceUi.EarPiece
            is AudioOutputDevice.LOUDSPEAKER -> AudioDeviceUi.LoudSpeaker
            is AudioOutputDevice.WIRED_HEADSET -> AudioDeviceUi.WiredHeadset
            is AudioOutputDevice.BLUETOOTH -> AudioDeviceUi.Bluetooth(
                id = identifier,
                name = name,
                connectionState = bluetoothConnectionStatus.mapToBluetoothDeviceState(),
                batteryLevel = batteryLevel
            )
        }
}