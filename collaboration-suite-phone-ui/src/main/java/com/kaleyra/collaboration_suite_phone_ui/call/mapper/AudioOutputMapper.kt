package com.kaleyra.collaboration_suite_phone_ui.call.mapper

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.BluetoothDeviceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object AudioOutputMapper {
    fun Flow<CallUI>.toCurrentAudioDeviceUi(): Flow<AudioDeviceUi?> =
        this.flatMapLatest { it.currentAudioOutputDevice }
            .map { it?.mapToAudioDeviceUi() }
            .distinctUntilChanged()

    fun Flow<CallUI>.toAudioDevicesUi(): Flow<List<AudioDeviceUi>> =
        this.flatMapLatest { it.audioOutputDevicesList }
            .map { list -> list.map { it.mapToAudioDeviceUi() } }
            .distinctUntilChanged()

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
        when (this) {
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVE -> BluetoothDeviceState.Active
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DISCONNECTED -> BluetoothDeviceState.Disconnected
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.AVAILABLE -> BluetoothDeviceState.Available
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DEACTIVATING -> BluetoothDeviceState.Deactivating
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING -> BluetoothDeviceState.Connecting
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTED -> BluetoothDeviceState.Connected
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVATING -> BluetoothDeviceState.Activating
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING_AUDIO -> BluetoothDeviceState.ConnectingAudio
            AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.PLAYING_AUDIO -> BluetoothDeviceState.PlayingAudio
            else -> BluetoothDeviceState.Failed

        }
}