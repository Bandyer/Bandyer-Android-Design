package com.kaleyra.collaboration_suite_phone_ui.call.component.ringing.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.callinfowidget.model.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class RingingUiState(
    val amIWaitingOthers: Boolean = false,
    val recording: RecordingTypeUi? = null,
    val answered: Boolean = false,
    override val video: VideoUi? = null,
    override val avatar: ImmutableUri? = null,
    override val participants: ImmutableList<String> = ImmutableList(listOf()),
    override val watermarkInfo: WatermarkInfo? = null,
    override val isLink: Boolean = false,
    override val isConnecting: Boolean = false,
    override val isVideoIncoming: Boolean = false
): PreCallUiState<RingingUiState> {

    override fun clone(
        video: VideoUi?,
        avatar: ImmutableUri?,
        participants: ImmutableList<String>,
        watermarkInfo: WatermarkInfo?,
        isLink: Boolean,
        isConnecting: Boolean,
        isVideoIncoming: Boolean
    ): RingingUiState {
        return copy(
            video = video,
            avatar = avatar,
            participants = participants,
            watermarkInfo = watermarkInfo,
            isLink = isLink,
            isConnecting = isConnecting,
            isVideoIncoming = isVideoIncoming
        )
    }
}