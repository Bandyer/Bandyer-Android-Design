package com.kaleyra.video_sdk.chat.appbar.model

import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

data class ChatParticipantsState(
    val online: ImmutableList<String> = ImmutableList(),
    val typing: ImmutableList<String> = ImmutableList(),
    val offline: ImmutableList<String> = ImmutableList()
)