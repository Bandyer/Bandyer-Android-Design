package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model

import androidx.compose.runtime.Immutable

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