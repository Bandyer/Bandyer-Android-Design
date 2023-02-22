package com.kaleyra.collaboration_suite_core_ui.call

import android.content.Context
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface StreamsVideoViewDelegate {
    fun setStreamsVideoView(context: Context, participants: Flow<CallParticipants>, scope: CoroutineScope) {
        participants
            .map { it.list }
            .flatMapLatest { participantsList ->
                participantsList.map { it.streams }.merge()
            }
            .flatMapLatest { it.map { stream -> stream.video }.merge() }
            .onEach { video ->
                if (video == null || video.view.value != null) return@onEach
                video.view.value = VideoStreamView(context.applicationContext)
            }
            .launchIn(scope)
    }
}