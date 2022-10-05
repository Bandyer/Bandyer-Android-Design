package com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import kotlinx.coroutines.flow.StateFlow

interface ChatUiViewModel {

    val uiState: StateFlow<ChatUiState>

    fun sendMessage(text: String)

    fun typing()

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun showCall()
}