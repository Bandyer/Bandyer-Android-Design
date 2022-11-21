package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

val mockAudioDevices = ImmutableList(
    listOf(
        AudioDevice.Bluetooth(
            id = "id",
            name = "Custom device",
            connectionState = BluetoothDeviceState.Active,
            batteryLevel = 75
        ),
        AudioDevice.LoudSpeaker,
        AudioDevice.EarPiece,
        AudioDevice.WiredHeadset,
        AudioDevice.Muted
    )
)