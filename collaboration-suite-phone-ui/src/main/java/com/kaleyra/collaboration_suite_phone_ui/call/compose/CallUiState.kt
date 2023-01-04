package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
data class CallUiState(
    val callInfo: CallInfoUi = CallInfoUi(),
    val thumbnailStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val featuredStream: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val groupCall: Boolean = false
)