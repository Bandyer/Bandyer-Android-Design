package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object ParticipantsMapper {

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