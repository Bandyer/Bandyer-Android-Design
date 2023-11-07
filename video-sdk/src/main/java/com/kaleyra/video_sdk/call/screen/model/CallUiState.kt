package com.kaleyra.video_sdk.call.screen.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.call.recording.model.RecordingUi
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

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