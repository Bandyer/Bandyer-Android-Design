package com.kaleyra.collaboration_suite_phone_ui.chat.screen.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.common.uistate.UiState

@Immutable
data class ChatUiState(
    val info: ChatInfo = ChatInfo("", null),
    val state: ChatState = ChatState.None,
    val actions: ImmutableSet<ChatAction> = ImmutableSet(setOf()),
    val conversationState: ConversationUiState = ConversationUiState(),
    val isInCall: Boolean = false
): UiState