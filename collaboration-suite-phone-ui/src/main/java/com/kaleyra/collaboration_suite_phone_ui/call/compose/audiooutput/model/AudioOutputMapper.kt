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
            is AudioOutputDevice.None -> AudioDeviceUi.Muted
            is AudioOutputDevice.Earpiece -> AudioDeviceUi.EarPiece
            is AudioOutputDevice.Loudspeaker -> AudioDeviceUi.LoudSpeaker
            is AudioOutputDevice.WiredHeadset -> AudioDeviceUi.WiredHeadset
            is AudioOutputDevice.Bluetooth -> AudioDeviceUi.Bluetooth(
                id = identifier,
                name = name,
                connectionState = bluetoothConnectionStatus.mapToBluetoothDeviceState(),
                batteryLevel = batteryLevel
            )
        }

    fun AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.mapToBluetoothDeviceState(): BluetoothDeviceState =
        when(this) {
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.FAILED -> BluetoothDeviceState.Failed
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DISCONNECTED -> BluetoothDeviceState.Disconnected
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.AVAILABLE -> BluetoothDeviceState.Available
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DEACTIVATING -> BluetoothDeviceState.Deactivating
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING -> BluetoothDeviceState.Connecting
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTED -> BluetoothDeviceState.Connected
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVATING -> BluetoothDeviceState.Activating
            else -> BluetoothDeviceState.Active
//            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.CONNECTING_AUDIO -> BluetoothDeviceState.ConnectingAudio
//            AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.PLAYING_AUDIO -> BluetoothDeviceState.PlayingAudio
        }
}