package com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel

import android.net.Uri
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.*
import kotlinx.coroutines.flow.StateFlow

interface ChatUiViewModel {

    val uiState: StateFlow<ChatUiState>

    fun readAllMessages()

    fun sendMessage(text: String)

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun call(callType: CallType)

    fun showCall()
}

data class ChatUiState(
    val info: ChatInfo = ChatInfo("", Uri.EMPTY),
    val state: ChatState = ChatState.None,
    val actions: Set<ChatAction> = setOf(),
    val conversationState: ConversationUiState = ConversationUiState(),
    val isInCall: Boolean = false
)

data class ConversationUiState(
    val areMessagesInitialized: Boolean = false,
    val conversationItems: List<ConversationItem> = emptyList(),
    val unseenMessagesCount: Int = 0
)