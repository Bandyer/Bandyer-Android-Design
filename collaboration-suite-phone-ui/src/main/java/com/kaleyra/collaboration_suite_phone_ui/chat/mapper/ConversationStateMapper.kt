package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite.conversation.Conversation
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.otherParticipantState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.typingEvents
import com.kaleyra.video_networking.connector.Connector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

object ConversationStateMapper {

    fun Flow<Conversation>.toChatState(participants: Flow<ChatParticipants>): Flow<ChatState> {
        var previousConversationState: Connector.State? = null

        return combine(
            participants.typingEvents(),
            this.flatMapLatest { it.state },
            participants.otherParticipantState()
        ) { event, conversationState, participantState ->
            when {
                conversationState is Connector.State.Connecting && previousConversationState is Connector.State.Connected -> ChatState.NetworkState.Offline
                conversationState is Connector.State.Connecting -> ChatState.NetworkState.Connecting
                conversationState is Connector.State.Connected && participantState is ChatParticipant.State.Joined.Online && event is ChatParticipant.Event.Typing.Idle -> ChatState.UserState.Online
                conversationState is Connector.State.Connected && participantState is ChatParticipant.State.Joined.Offline && event is ChatParticipant.Event.Typing.Idle -> {
                    val lastLogin = participantState.lastLogin
                    ChatState.UserState.Offline(
                        if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At) lastLogin.date.time
                        else null
                    )
                }
                conversationState is Connector.State.Connected && event is ChatParticipant.Event.Typing.Started -> ChatState.UserState.Typing
                else -> ChatState.None
            }.also {
                previousConversationState = conversationState
            }
        }
    }
}