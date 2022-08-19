package com.kaleyra.collaboration_suite_phone_ui.chat

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal interface ChatComposeViewModel {

    val stateInfo: SharedFlow<StateInfo>

    val chatActions: SharedFlow<Set<Action>>

    val conversationItems: SharedFlow<List<ConversationItem>>

    val unseenMessagesCount: SharedFlow<Int>

    val isCallActive: SharedFlow<Boolean>

    val areMessagesFetched: StateFlow<Boolean>

    fun readAllMessages()

    fun sendMessage(text: String)

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun call(callType: CallType)

    fun showCall()
}