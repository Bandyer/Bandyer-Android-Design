package com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.Recording
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo

internal data class RingingUiState(
    val stream: StreamUi? = null,
    val participants: List<String> = listOf(),
    val watermarkInfo: WatermarkInfo? = null,
    val isGroupCall: Boolean = false,
    val recording: Recording? = null,
    override val userMessage: String? = null
) : UiState