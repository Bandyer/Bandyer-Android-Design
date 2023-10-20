package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ConnectionState {

    data object Connecting : ConnectionState()

    data object Connected: ConnectionState()

    data object Offline : ConnectionState()

    data object Error : ConnectionState()

    data object Unknown : ConnectionState()
}

