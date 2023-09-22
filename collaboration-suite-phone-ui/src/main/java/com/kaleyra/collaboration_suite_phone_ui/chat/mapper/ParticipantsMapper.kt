package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ParticipantState
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

typealias ParticipantDetails = Pair<String, ImmutableUri>

object ParticipantsMapper {

    fun Flow<ChatParticipants>.toChatParticipantsDetails(): Flow<ImmutableMap<String, ParticipantDetails>> =
        flatMapLatest { chatParticipants ->
            val participantsList = chatParticipants.list
            val users = mutableMapOf<String, ParticipantDetails>()
            if (participantsList.isEmpty()) flowOf(ImmutableMap())
            else participantsList
                .map { participant ->
                    combine(
                        participant.combinedDisplayName,
                        participant.combinedDisplayImage.map { ImmutableUri(it ?: Uri.EMPTY) }
                    ) { name, image -> Triple(participant.userId, name ?: "", image) }
                }
                .merge()
                .transform { (userId, name, image) ->
                    users[userId] = ParticipantDetails(name, image)
                    val values = users.values.toList()
                    if (values.size == participantsList.size) {
                        emit(ImmutableMap(users))
                    }
                }
        }.distinctUntilChanged()

    fun Flow<ChatParticipants>.toRecipientDetails(): Flow<ParticipantDetails> =
        map { it.others.first() }
            .flatMapLatest { participant ->
            combine(participant.combinedDisplayName, participant.combinedDisplayImage.map { ImmutableUri(it ?: Uri.EMPTY) }) { name, image ->
                ParticipantDetails(name ?: "", image)
            }
        }

    fun Flow<ChatParticipants>.toParticipantsState(): Flow<ImmutableList<ParticipantState>> {
        val participantsState = mutableMapOf<String, ParticipantState>()
        return flatMapLatest { participants: ChatParticipants ->
            val others = participants.others
            others
                .map { participant ->
                    combine(
                        participant.state,
                        participant.events.filterIsInstance<ChatParticipant.Event.Typing>()
                    ) { participantState, typingEvent ->
                        when {
                            typingEvent is ChatParticipant.Event.Typing.Started -> ParticipantState.Typing
                            participantState is ChatParticipant.State.Joined.Online -> ParticipantState.Online
                            participantState is ChatParticipant.State.Joined.Offline -> {
                                val lastLogin = participantState.lastLogin
                                ParticipantState.Offline(
                                    if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At) lastLogin.date.time
                                    else null
                                )
                            }

                            else -> null
                        }?.let { participant.userId to it }
                    }.filterNotNull()
                }
                .merge()
                .transform { (participantId, participantState) ->
                    participantsState[participantId] = participantState
                    val values = participantsState.values.toList()
                    if (values.size == others.size) {
                        emit(ImmutableList(participantsState.values.toList()))
                    }
                }
        }
    }

    fun Flow<ChatParticipants>.isGroupChat(): Flow<Boolean> =
       map { it.others.size > 1 }.distinctUntilChanged()
}