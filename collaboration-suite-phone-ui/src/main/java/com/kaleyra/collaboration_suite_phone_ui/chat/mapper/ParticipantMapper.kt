package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

object ParticipantsMapper {

    fun ChatParticipants.isGroupChat(): Boolean = others.size > 1

    suspend fun ChatParticipants.toParticipantsDetails(): ImmutableMap<String, ChatParticipantDetails> {
        return if (list.isEmpty()) ImmutableMap()
        else {
            coroutineScope {
                val participantsDetails = list
                    .map {
                        val name = async { it.combinedDisplayName.filterNotNull().first() }
                        val image = async { it.combinedDisplayImage.filterNotNull().first() }
                        Triple(it, name, image)
                    }
                    .map { (participant, name, image) ->
                        participant.userId to ChatParticipantDetails(
                            username = name.await(),
                            image = ImmutableUri(image.await()),
                            state = participant.toChatParticipantState()
                        )
                    }
                ImmutableMap(participantsDetails.toMap())
            }
        }
    }

    fun ChatParticipant.toChatParticipantState(): Flow<ChatParticipantState> {
        return combine(
            state,
            events.filterIsInstance<ChatParticipant.Event.Typing>()
        ) { participantState, typingEvent ->
            when {
                typingEvent is ChatParticipant.Event.Typing.Started -> ChatParticipantState.Typing
                participantState is ChatParticipant.State.Joined.Online -> ChatParticipantState.Online
                participantState is ChatParticipant.State.Joined.Offline -> {
                    val lastLogin = participantState.lastLogin
                    ChatParticipantState.Offline(
                        if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At) lastLogin.date.time
                        else null
                    )
                }

                else -> ChatParticipantState.Unknown
            }
        }.distinctUntilChanged()
    }

    suspend fun ChatParticipants.toOtherParticipantsState(): Flow<ChatParticipantsState> {
        return coroutineScope {
            if (others.isEmpty()) flowOf(ChatParticipantsState())
            else {
                val states = others
                    .map { participant ->
                        val username = async { participant.combinedDisplayName.filterNotNull().first() }
                        Pair(participant, username)
                    }
                    .map { (participant, deferredUsername) ->
                        participant.toChatParticipantState().map { Triple(participant.userId, deferredUsername.await(), it) }
                    }

                val participantsState = mutableMapOf<String, Pair<String, ChatParticipantState>>()
                states
                    .merge()
                    .transform { (userId, username, state) ->
                        participantsState[userId] = Pair(username, state)
                        if (others.size == participantsState.keys.size) {
                            emit(participantsState.values.toList().mapToChatParticipantsState())
                        }
                    }
                    .distinctUntilChanged()
            }
        }
    }

    fun List<Pair<String, ChatParticipantState>>.mapToChatParticipantsState(): ChatParticipantsState {
        val online = mutableListOf<String>()
        val typing = mutableListOf<String>()
        val offline = mutableListOf<String>()
        forEach { (username, state) ->
            when (state) {
                is ChatParticipantState.Online -> online.add(username)
                is ChatParticipantState.Typing -> typing.add(username)
                else -> offline.add(username)
            }
        }
        return ChatParticipantsState(
            online = ImmutableList(online),
            typing = ImmutableList(typing),
            offline = ImmutableList(offline)
        )
    }
}