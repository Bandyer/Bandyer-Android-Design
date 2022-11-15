package com.kaleyra.collaboration_suite_phone_ui.call.compose.model

import android.net.Uri
import androidx.compose.runtime.Immutable

sealed interface WhiteboardUpload {
    data class Uploading(val progress: Float): WhiteboardUpload
    object Error: WhiteboardUpload
}

@Immutable
enum class ScreenShareTarget {
    Device,
    Application
}

enum class BluetoothDeviceState {
    Available,
    Connecting,
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

@Immutable
sealed interface Transfer {

    val fileName: String

    val fileType: FileType

    val fileSize: Long

    val sender: String

    val progress: Float

    val time: Long

    val uri: Uri

    val state: State

    val onActionClick: () -> Unit

    val onClick: () -> Unit

    data class Upload(
        override val fileName: String,
        override val fileType: FileType,
        override val fileSize: Long,
        override val sender: String,
        override val progress: Float,
        override val time: Long,
        override val uri: Uri,
        override val state: State,
        override val onActionClick: () -> Unit,
        override val onClick: () -> Unit
    ) : Transfer

    data class Download(
        override val fileName: String,
        override val fileType: FileType,
        override val fileSize: Long,
        override val sender: String,
        override val progress: Float,
        override val time: Long,
        override val uri: Uri,
        override val state: State,
        override val onActionClick: () -> Unit,
        override val onClick: () -> Unit
    ) : Transfer

    enum class State {
        Available,
        Pending,
        InProgress,
        Success,
        Error,
        Cancelled
    }

    enum class FileType {
        Media,
        Archive,
        Miscellaneous
    }
}

@Immutable
sealed interface AudioDevice {

    val id: String

    val isPlaying: Boolean

    data class Bluetooth(
        override val id: String,
        override val isPlaying: Boolean,
        val name: String?,
        val connectionState: BluetoothDeviceState,
        val batteryLevel: Int?
    ) : AudioDevice

    data class LoudSpeaker(
        override val id: String,
        override val isPlaying: Boolean
    ) : AudioDevice

    data class EarPiece(
        override val id: String,
        override val isPlaying: Boolean
    ) : AudioDevice

    data class WiredHeadset(
        override val id: String,
        override val isPlaying: Boolean
    ) : AudioDevice

    data class Muted(
        override val id: String,
        override val isPlaying: Boolean
    ) : AudioDevice
}

@Immutable
sealed interface CallAction {

    val isEnabled: Boolean

    sealed interface Clickable : CallAction {
        val onClick: () -> Unit
    }

    sealed interface Toggleable : CallAction {
        val isToggled: Boolean

        val onToggle: (Boolean) -> Unit
    }

    data class Camera(
        override val isToggled: Boolean,
        override val isEnabled: Boolean,
        override val onToggle: (Boolean) -> Unit
    ) : Toggleable

    data class Microphone(
        override val isToggled: Boolean,
        override val isEnabled: Boolean,
        override val onToggle: (Boolean) -> Unit
    ) : Toggleable

    data class SwitchCamera(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class HangUp(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class Chat(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class Whiteboard(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class FileShare(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class Audio(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class ScreenShare(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable
}