package com.kaleyra.collaboration_suite_phone_ui.call.compose.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BottomSheetContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.targetState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

val mockCallActions = ImmutableList(
    listOf(
        CallAction.Microphone(isToggled = false, isEnabled = true) {},
        CallAction.Camera(isToggled = false, isEnabled = false) {},
        CallAction.SwitchCamera(true) {},
        CallAction.HangUp(true) {},
        CallAction.Chat(true) {},
        CallAction.Whiteboard(true) {},
        CallAction.Audio(true) {
            targetState = BottomSheetContent.AudioRoute
        },
        CallAction.FileShare(true) {},
        CallAction.ScreenShare(true) { targetState = BottomSheetContent.ScreenShare }
    )
)

val mockAudioDevices = ImmutableList(
    listOf(
        AudioDevice.Bluetooth(
            id = "id",
            isPlaying = true,
            name = "Custom device",
            connectionState = BluetoothDeviceState.ACTIVE,
            batteryLevel = 75
        ),
        AudioDevice.LoudSpeaker(id = "id2", isPlaying = false),
        AudioDevice.EarPiece(id = "id3", isPlaying = false),
        AudioDevice.WiredHeadset(id = "id4", isPlaying = false),
        AudioDevice.Muted(id = "id5", isPlaying = false)
    )
)