package com.kaleyra.collaboration_suite_phone_ui.chat

import kotlinx.coroutines.flow.Flow

internal interface ChatComposeViewModel {

    val stateInfo: Flow<StateInfo>

    val chatActions: Flow<Set<Action>>

    val conversationItems: Flow<List<ConversationItem>>

    val unseenMessagesCount: Flow<Int>

    val isCallActive: Flow<Boolean>

    val areMessagesFetched: Flow<Boolean>

    fun readAllMessages()

    fun sendMessage(text: String)

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun call(callType: CallType)

    fun showCall()
}