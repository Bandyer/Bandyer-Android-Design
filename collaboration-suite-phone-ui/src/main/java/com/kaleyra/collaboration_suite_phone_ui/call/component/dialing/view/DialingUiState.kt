package com.kaleyra.collaboration_suite_phone_ui.call.component.dialing.view

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.callinfowidget.model.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class DialingUiState(
    override val video: VideoUi? = null,
    override val avatar: ImmutableUri? = null,
    override val participants: ImmutableList<String> = ImmutableList(listOf()),
    override val watermarkInfo: WatermarkInfo? = null,
    override val isLink: Boolean = false,
    override val isConnecting: Boolean = false,
    override val isVideoIncoming: Boolean = false
): PreCallUiState<DialingUiState> {

    override fun clone(
        video: VideoUi?,
        avatar: ImmutableUri?,
        participants: ImmutableList<String>,
        watermarkInfo: WatermarkInfo?,
        isLink: Boolean,
        isConnecting: Boolean,
        isVideoIncoming: Boolean
    ): DialingUiState {
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