package com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.UserMessageViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import kotlinx.coroutines.flow.StateFlow

interface ChatUiViewModel : UserMessageViewModel {

    val uiState: StateFlow<ChatUiState>

    fun sendMessage(text: String)

    fun typing()

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun showCall()
}