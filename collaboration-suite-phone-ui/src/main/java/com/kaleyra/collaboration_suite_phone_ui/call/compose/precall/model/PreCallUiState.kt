package com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessages
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class PreCallUiState(
    val video: VideoUi? = null,
    val avatar: ImmutableUri? = null,
    val participants: ImmutableList<String> = ImmutableList(listOf()),
    val watermarkInfo: WatermarkInfo? = null,
    val recording: RecordingTypeUi? = null,
    val isLink: Boolean = false,
    val isConnecting: Boolean = false,
    override val userMessages: UserMessages = UserMessages()
) : UiState