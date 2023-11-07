package com.kaleyra.video_sdk.chat.appbar.model

import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Stable
data class ChatParticipantDetails(
    val username: String = "",
    val image: ImmutableUri = ImmutableUri(),
    val state: Flow<ChatParticipantState> = flowOf()
)