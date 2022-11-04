package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Immutable

@Immutable
enum class ScreenShare {
    DEVICE,
    APPLICATION
}

enum class BluetoothDeviceState {
    AVAILABLE,
    CONNECTING,
    CONNECTED,
    ACTIVATING,
    ACTIVE,
    DEACTIVATING,
    DISCONNECTED,
    FAILED
}

internal fun BluetoothDeviceState.isConnecting() =
    this == BluetoothDeviceState.ACTIVE || this == BluetoothDeviceState.CONNECTING || this == BluetoothDeviceState.ACTIVATING

internal fun BluetoothDeviceState.isConnected() =
    this == BluetoothDeviceState.ACTIVE || this == BluetoothDeviceState.CONNECTED || this == BluetoothDeviceState.ACTIVATING

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

    data class FileSharing(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class Audio(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable

    data class ScreenSharing(
        override val isEnabled: Boolean,
        override val onClick: () -> Unit
    ) : Clickable
}