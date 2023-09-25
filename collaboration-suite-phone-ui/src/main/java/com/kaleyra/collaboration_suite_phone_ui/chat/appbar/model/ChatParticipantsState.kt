package com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model

import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

data class ChatParticipantsState(
    val online: ImmutableList<String> = ImmutableList(),
    val typing: ImmutableList<String> = ImmutableList()
)