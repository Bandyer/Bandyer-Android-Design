package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ChatState {
    sealed class NetworkState : ChatState() {
        object Connecting : NetworkState()
        object Offline : NetworkState()
    }

    sealed class UserState : ChatState() {
        object Online : UserState()
        data class Offline(val timestamp: Long?) : UserState()
        object Typing : UserState()
    }

    object None : ChatState()
}