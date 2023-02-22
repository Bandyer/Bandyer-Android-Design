package com.kaleyra.collaboration_suite_core_ui.call

import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface StreamsOpeningDelegate {
    fun openParticipantsStreams(participants: Flow<CallParticipants>, scope: CoroutineScope) {
        participants
            .map { it.list }
            .flatMapLatest { participantsList ->
                participantsList.map { it.streams }.merge()
            }
            .onEach { it.forEach { stream -> stream.open() } }
            .launchIn(scope)
    }
}