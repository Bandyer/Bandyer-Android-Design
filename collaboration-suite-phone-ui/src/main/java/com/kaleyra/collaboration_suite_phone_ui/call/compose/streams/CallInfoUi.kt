package com.kaleyra.collaboration_suite_phone_ui.call.compose.streams

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Recording

@Immutable
data class CallInfoUi(
    val callState: CallState = CallState.Disconnected,
    val otherParticipants: List<String> = listOf(),
    val watermarkInfo: WatermarkInfo? = null,
    val recording: Recording? = null
)