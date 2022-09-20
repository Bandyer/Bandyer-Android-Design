package com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.*
import kotlinx.coroutines.flow.StateFlow

interface ChatUiViewModel {

    val uiState: StateFlow<ChatUiState>

    fun sendMessage(text: String)

    fun typing()

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun call(callType: CallType)

    fun showCall()
}

@Immutable
data class ChatUiState(
    val info: ChatInfo = ChatInfo("", Uri.EMPTY),
    val state: ChatState = ChatState.None,
    val actions: Set<ChatAction> = setOf(),
    val conversationState: ConversationUiState = ConversationUiState(),
    val isInCall: Boolean = false
)

@Immutable
data class ConversationUiState(
    val areAllMessagesFetched: Boolean = false,
    val conversationItems: List<ConversationItem>? = null,
    val unreadMessagesCount: Int = 0
)