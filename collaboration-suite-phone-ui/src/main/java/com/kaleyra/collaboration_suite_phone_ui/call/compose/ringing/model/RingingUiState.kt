package com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi

internal data class RingingUiState(
    val stream: StreamUi? = null,
    val callInfo: CallInfoUi = CallInfoUi(),
    val isGroupCall: Boolean = false,
    override val userMessage: String? = null
) : UiState