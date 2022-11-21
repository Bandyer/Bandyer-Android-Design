package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model

import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

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