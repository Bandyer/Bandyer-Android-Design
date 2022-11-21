package com.kaleyra.collaboration_suite_phone_ui.call.compose.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.BluetoothDeviceState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

val mockCallActions = ImmutableList(
    listOf(
        CallAction.Microphone(isToggled = false, isEnabled = true) {},
        CallAction.Camera(isToggled = false, isEnabled = false) {},
        CallAction.SwitchCamera(true) {},
        CallAction.HangUp(true) {},
        CallAction.Chat(true) {},
        CallAction.Whiteboard(true) {},
        CallAction.Audio(true) {},
        CallAction.FileShare(true) {},
        CallAction.ScreenShare(true) {}
    )
)

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

val mockUploadTransfer = TransferUi(
    id = "1",
    file = FileUi(
        name = "upload.txt",
        type = FileUi.Type.Media,
        size = 23333L
    ),
    sender = "Mario",
    time = 324234L,
    state = TransferUi.State.InProgress(progress = .7f),
    type = TransferUi.Type.Upload
)

val mockDownloadTransfer = TransferUi(
    id = "2",
    file = FileUi(
        name = "download.txt",
        type = FileUi.Type.Miscellaneous,
        size = 40000L
    ),
    sender = "Keanu",
    time = 3254234L,
    state = TransferUi.State.InProgress(progress = .4f),
    type = TransferUi.Type.Download
)