package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputMapper.mapToBluetoothDeviceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object AudioMapper {
    fun Flow<CallUI>.toCurrentAudioDeviceUi(): Flow<AudioDeviceUi?> =
        flatMapLatest { it.currentAudioOutputDevice }.map { it?.mapToAudioDeviceUi() }

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