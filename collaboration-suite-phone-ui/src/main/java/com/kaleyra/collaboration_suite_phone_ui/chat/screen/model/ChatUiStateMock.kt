package com.kaleyra.collaboration_suite_phone_ui.chat.screen.model

import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock.mockConversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.mockActions

val mockChatUiState = ChatUiState(
    info = ChatInfo("John Smith", null),
    state = ConnectionState.NetworkState.Connecting,
    actions = mockActions,
    conversationState = mockConversationState,
    isInCall = true
)