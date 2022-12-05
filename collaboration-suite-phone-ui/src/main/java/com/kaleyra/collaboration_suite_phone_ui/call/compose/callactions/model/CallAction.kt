package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CallAction {

    val isEnabled: Boolean

    sealed interface Toggleable : CallAction {
        val isToggled: Boolean
    }

    data class Camera(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    data class Microphone(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    data class SwitchCamera(override val isEnabled: Boolean = true) : CallAction

    data class HangUp(override val isEnabled: Boolean = true) : CallAction

    data class Chat(override val isEnabled: Boolean = true) : CallAction

    data class Whiteboard(override val isEnabled: Boolean = true) : CallAction

    data class FileShare(override val isEnabled: Boolean = true) : CallAction

    data class Audio(override val isEnabled: Boolean = true, val device: AudioActionDevice = AudioActionDevice.Muted) : CallAction

    data class ScreenShare(override val isEnabled: Boolean = true) : CallAction
}

enum class AudioActionDevice {
    LoudSpeaker,
    Earpiece,
    WiredHeadset,
    Bluetooth,
    Muted
}