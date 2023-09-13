package com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model

import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

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