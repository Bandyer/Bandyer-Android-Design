package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ChatAction(open val onClick: () -> Unit) {
    data class AudioCall(override val onClick: () -> Unit) : ChatAction(onClick)
    data class AudioUpgradableCall(override val onClick: () -> Unit) : ChatAction(onClick)
    data class VideoCall(override val onClick: () -> Unit) : ChatAction(onClick)
}