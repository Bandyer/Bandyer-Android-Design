package com.kaleyra.video_sdk.call.callactions.model

import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

val mockCallActions = ImmutableList(
    listOf(
        CallAction.Microphone(isToggled = false, isEnabled = true),
        CallAction.Camera(isToggled = false, isEnabled = false),
        CallAction.SwitchCamera(true),
        CallAction.HangUp(true),
        CallAction.Chat(true),
        CallAction.Whiteboard(true),
        CallAction.Audio(
            true,
            AudioDeviceUi.Bluetooth(
                id = "id",
                name = null,
                connectionState = BluetoothDeviceState.Connected,
                batteryLevel = null
            )
        ),
        CallAction.FileShare(true),
        CallAction.ScreenShare(true),
        CallAction.VirtualBackground(true)
    )
)