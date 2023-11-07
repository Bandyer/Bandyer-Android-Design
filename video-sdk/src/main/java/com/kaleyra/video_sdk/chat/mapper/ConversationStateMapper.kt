package com.kaleyra.video_sdk.chat.mapper

import com.kaleyra.video.State
import com.kaleyra.video.conversation.Conversation
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
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