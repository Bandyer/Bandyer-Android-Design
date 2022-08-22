package com.kaleyra.collaboration_suite_phone_ui.chat

import android.net.Uri
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
    val conversationItems: List<ConversationItem> = emptyList(),
    val unseenMessagesCount: Int = 0,
    val isInCall: Boolean = false,
    val areMessagesFetched: Boolean = false,
)