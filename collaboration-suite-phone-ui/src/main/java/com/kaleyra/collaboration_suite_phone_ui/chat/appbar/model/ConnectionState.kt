package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

@Immutable
sealed class ConnectionState {

    object Connecting : ConnectionState()

    object Connected: ConnectionState()

    object Offline : ConnectionState()

    object Undefined : ConnectionState()
}

