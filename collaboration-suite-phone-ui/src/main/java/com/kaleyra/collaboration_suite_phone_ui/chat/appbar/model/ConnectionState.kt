package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ConnectionState {

    object Connecting : ConnectionState()

    object Connected: ConnectionState()

    object Offline : ConnectionState()

    object Unknown : ConnectionState()
}

