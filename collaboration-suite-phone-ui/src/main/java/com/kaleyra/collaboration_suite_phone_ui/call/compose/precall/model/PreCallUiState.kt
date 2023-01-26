package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Recording
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo

// TODO change participants to ImmutableList
@Immutable
data class PreCallUiState(
    val stream: StreamUi? = null,
    val participants: List<String> = listOf(),
    val watermarkInfo: WatermarkInfo? = null,
    val isGroupCall: Boolean = false,
    val recording: Recording? = null,
    override val userMessage: String? = null
) : UiState