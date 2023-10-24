package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.State
import com.kaleyra.collaboration_suite.conversation.Conversation
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object ConversationStateMapper {

    fun Flow<Conversation>.toConnectionState(): Flow<ConnectionState> {
        var previousConversationState: State? = null

        return this
            .flatMapLatest { it.state }
            .map { conversationState ->
                when {
                    conversationState is State.Connecting && previousConversationState is State.Connected -> ConnectionState.Offline
                    conversationState is State.Connecting                                                 -> ConnectionState.Connecting
                    conversationState is State.Connected -> ConnectionState.Connected
                    conversationState is State.Disconnected.Error -> ConnectionState.Error
                    else -> ConnectionState.Unknown
                }.also {
                    previousConversationState = conversationState
                }
            }
    }
}