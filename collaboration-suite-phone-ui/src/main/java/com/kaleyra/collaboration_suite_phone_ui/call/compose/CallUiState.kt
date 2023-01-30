package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class CallUiState(
    val callState: CallState = CallState.Disconnected,
    val thumbnailStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val featuredStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val watermarkInfo: WatermarkInfo? = null,
    val isGroupCall: Boolean = false,
    val isRecording: Boolean = false,
    val isCameraPermissionRequired: Boolean = true,
    val isMicPermissionRequired: Boolean = true,
    override val userMessage: String? = null
) : UiState