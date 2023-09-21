package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conversation.Chat
import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

object ParticipantsMapper {

    fun Flow<ChatParticipants>.toChatParticipantUserDetails(): Flow<ImmutableMap<String, ParticipantDetails>> =
        flatMapLatest { chatParticipants ->
            val participantsList = chatParticipants.list
            val users = mutableMapOf<String, ParticipantDetails>()
            participantsList
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

    fun Flow<Chat>.isGroupCall(companyId: Flow<String>): Flow<Boolean> =
        combine(this.flatMapLatest { it.participants }, companyId) { participants, companyId ->
            participants.others.filter { it.userId != companyId }.size > 1
        }.distinctUntilChanged()

    fun Flow<ChatParticipants>.toChatInfo(): Flow<ChatInfo> {
        val participant = otherParticipant()
        return combine(participant.flatMapLatest { it.combinedDisplayName }, participant.flatMapLatest { it.combinedDisplayImage }) { name, image ->
            ChatInfo(name = name ?: "", image = ImmutableUri(image ?: Uri.EMPTY))
        }
    }

    private fun Flow<ChatParticipants>.otherParticipant(): Flow<ChatParticipant> =
        map { it.others.first() }

    fun Flow<ChatParticipants>.typingEvents(): Flow<ChatParticipant.Event> =
        otherParticipant().flatMapLatest { it.events.filterIsInstance<ChatParticipant.Event.Typing>() }

    fun Flow<ChatParticipants>.otherParticipantState(): Flow<ChatParticipant.State> =
        otherParticipant().flatMapLatest { it.state }
}