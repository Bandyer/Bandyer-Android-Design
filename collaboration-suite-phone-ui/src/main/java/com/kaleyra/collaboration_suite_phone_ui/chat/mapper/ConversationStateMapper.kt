package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conversation.Conversation
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object ConversationStateMapper {

    fun Flow<Conversation>.toConnectionState(): Flow<ConnectionState> {
        var previousConversationState: Conversation.State? = null

        return this
            .flatMapLatest { it.state }
            .map { conversationState ->
                when {
                    conversationState is Conversation.State.Connecting && previousConversationState is Conversation.State.Connected -> ConnectionState.Offline
                    conversationState is Conversation.State.Connecting -> ConnectionState.Connecting
                    conversationState is Conversation.State.Connected -> ConnectionState.Connected
                    else -> ConnectionState.Undefined
                }.also {
                    previousConversationState = conversationState
                }
            }
    }
}