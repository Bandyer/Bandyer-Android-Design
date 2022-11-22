package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CallAction {

    val isEnabled: Boolean

    sealed interface Toggleable : CallAction {
        val isToggled: Boolean
    }

    data class Camera(override val isEnabled: Boolean, override val isToggled: Boolean) : Toggleable

    data class Microphone(override val isEnabled: Boolean, override val isToggled: Boolean) : Toggleable

    data class SwitchCamera(override val isEnabled: Boolean) : CallAction

    data class HangUp(override val isEnabled: Boolean) : CallAction

    data class Chat(override val isEnabled: Boolean) : CallAction

    data class Whiteboard(override val isEnabled: Boolean) : CallAction

    data class FileShare(override val isEnabled: Boolean) : CallAction

    data class Audio(override val isEnabled: Boolean) : CallAction

    data class ScreenShare(override val isEnabled: Boolean) : CallAction
}