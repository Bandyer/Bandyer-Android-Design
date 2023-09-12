package com.kaleyra.collaboration_suite_phone_ui.call.component.screen.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.common.uistate.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.model.RecordingUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.callinfowidget.model.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class CallUiState(
    val callState: CallStateUi = CallStateUi.Disconnected,
    val thumbnailStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val featuredStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val fullscreenStream: StreamUi? = null,
    val watermarkInfo: WatermarkInfo? = null,
    val recording: RecordingUi? = null,
    val isAudioOnly: Boolean = false,
    val isGroupCall: Boolean = false,
    val amIAlone: Boolean = false,
    val showFeedback: Boolean = false,
    val shouldAutoHideSheet: Boolean = false
) : UiState