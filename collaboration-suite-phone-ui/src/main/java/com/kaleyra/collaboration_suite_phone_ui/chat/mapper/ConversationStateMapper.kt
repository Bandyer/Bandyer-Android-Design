package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conversation.Conversation
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.video_networking.connector.Connector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object ConversationStateMapper {

    fun Flow<Conversation>.toConnectionState(): Flow<ConnectionState> {
        var previousConversationState: Connector.State? = null

        return this
            .flatMapLatest { it.state }
            .map { conversationState ->
                when {
                    conversationState is Connector.State.Connecting && previousConversationState is Connector.State.Connected -> ConnectionState.Offline
                    conversationState is Connector.State.Connecting -> ConnectionState.Connecting
                    conversationState is Connector.State.Connected -> ConnectionState.Connected
                    else -> ConnectionState.Unknown
                }.also {
                    previousConversationState = conversationState
                }
            }
    }
}