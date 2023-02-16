package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.getAvailableAudioOutputDevices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal object AudioOutputMapper {

    fun Flow<Call>.toAudioDevicesUi(): Flow<List<AudioDeviceUi>> {
        return this
            .map { call ->
                call
                    .getAvailableAudioOutputDevices()
                    .map { it.mapToAudioDeviceUi() }
            }
    }

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

    fun AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.mapToBluetoothDeviceState(): BluetoothDeviceState =
        when(this) {
            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.FAILED -> BluetoothDeviceState.Failed
            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.DISCONNECTED -> BluetoothDeviceState.Disconnected
            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.AVAILABLE -> BluetoothDeviceState.Available
            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.DEACTIVATING -> BluetoothDeviceState.Deactivating
            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.CONNECTING -> BluetoothDeviceState.Connecting
            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.CONNECTED -> BluetoothDeviceState.Connected
            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.ACTIVATING -> BluetoothDeviceState.Activating
            else -> BluetoothDeviceState.Active
//            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.CONNECTING_AUDIO -> BluetoothDeviceState.ConnectingAudio
//            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.PLAYING_AUDIO -> BluetoothDeviceState.PlayingAudio
        }
}