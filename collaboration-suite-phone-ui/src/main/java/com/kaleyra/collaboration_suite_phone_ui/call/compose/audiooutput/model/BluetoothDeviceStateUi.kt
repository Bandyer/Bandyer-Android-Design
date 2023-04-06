package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

enum class BluetoothDeviceState {
    Available,
    Connecting,
    ConnectingAudio,
    PlayingAudio,
    Connected,
    Activating,
    Active,
    Deactivating,
    Disconnected,
    Failed
}

internal fun BluetoothDeviceState.isConnecting() =
    this == BluetoothDeviceState.Connecting || this == BluetoothDeviceState.ConnectingAudio

internal fun BluetoothDeviceState.isConnectedOrPlaying() =
    this == BluetoothDeviceState.Active || this == BluetoothDeviceState.Connected || this == BluetoothDeviceState.PlayingAudio