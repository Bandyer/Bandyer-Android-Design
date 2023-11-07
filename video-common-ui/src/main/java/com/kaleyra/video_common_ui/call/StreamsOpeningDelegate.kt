package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.CallParticipants
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