package com.kaleyra.collaboration_suite_phone_ui.chat.screen.model

import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock.mockConversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.mockActions
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.flowOf

val mockChatUiState = ChatUiState.OneToOne(
    recipientDetails = ChatParticipantDetails("John Smith", ImmutableUri(), flowOf()),
    connectionState = ConnectionState.Connecting,
    actions = mockActions,
    conversationState = mockConversationState,
    isInCall = true
)