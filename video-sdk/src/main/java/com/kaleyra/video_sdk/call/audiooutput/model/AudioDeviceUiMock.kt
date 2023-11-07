package com.kaleyra.video_sdk.call.audiooutput.model

import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

val mockAudioDevices = ImmutableList(
    listOf(
        AudioDeviceUi.Bluetooth(
            id = "id",
            name = "Custom device",
            connectionState = BluetoothDeviceState.Active,
            batteryLevel = 75
        ),
        AudioDeviceUi.LoudSpeaker,
        AudioDeviceUi.EarPiece,
        AudioDeviceUi.WiredHeadset,
        AudioDeviceUi.Muted
    )
)