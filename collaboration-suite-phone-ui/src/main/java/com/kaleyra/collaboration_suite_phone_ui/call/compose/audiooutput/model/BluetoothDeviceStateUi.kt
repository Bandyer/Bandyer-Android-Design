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
    this == BluetoothDeviceState.Active || this == BluetoothDeviceState.Connecting || this == BluetoothDeviceState.Activating

internal fun BluetoothDeviceState.isConnected() =
    this == BluetoothDeviceState.Active || this == BluetoothDeviceState.Connected || this == BluetoothDeviceState.Activating