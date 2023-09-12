package com.kaleyra.collaboration_suite_phone_ui.call.component.callactions.model

import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.BluetoothDeviceState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

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