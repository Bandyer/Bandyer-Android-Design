package com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatUiViewModel {

    val uiState: StateFlow<ChatUiState>

    val userMessage: Flow<UserMessage>

    fun sendMessage(text: String)

    fun typing()

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun showCall()
}