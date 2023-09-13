package com.kaleyra.collaboration_suite_phone_ui.chat.screen.model

import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock.mockConversationUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.mockActions

val mockChatUiState = ChatUiState(
    info = ChatInfo("John Smith", null),
    state = ChatState.NetworkState.Connecting,
    actions = mockActions,
    conversationState = mockConversationUiState,
    isInCall = true
)