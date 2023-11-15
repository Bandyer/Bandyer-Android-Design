package com.kaleyra.video_sdk.chat.screen.model

import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.conversation.model.mock.mockConversationState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.appbar.model.mockActions
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.flowOf

internal val mockChatUiState = ChatUiState.OneToOne(
    recipientDetails = ChatParticipantDetails("John Smith", ImmutableUri(), flowOf()),
    connectionState = ConnectionState.Connecting,
    actions = mockActions,
    conversationState = mockConversationState,
    isInCall = true
)