package com.kaleyra.video_sdk.call.audiooutput.model

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
    this == BluetoothDeviceState.Connecting || this == BluetoothDeviceState.ConnectingAudio || this == BluetoothDeviceState.Activating

internal fun BluetoothDeviceState.isConnectedOrPlaying() =
    this == BluetoothDeviceState.Active || this == BluetoothDeviceState.Connected || this == BluetoothDeviceState.PlayingAudio || this == BluetoothDeviceState.Activating || this == BluetoothDeviceState.Connecting || this == BluetoothDeviceState.ConnectingAudio