package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CallAction {

    val enabled: Boolean

    val onClick: () -> Unit

    data class Camera(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class Microphone(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class SwitchCamera(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class HungUp(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class Chat(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class Whiteboard(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class FileSharing(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class Audio(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction

    data class ScreenSharing(
        override val enabled: Boolean,
        override val onClick: () -> Unit
    ) : CallAction
}