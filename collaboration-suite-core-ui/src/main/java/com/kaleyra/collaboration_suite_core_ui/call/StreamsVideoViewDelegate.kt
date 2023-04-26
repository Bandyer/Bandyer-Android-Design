package com.kaleyra.collaboration_suite_core_ui.call

import android.content.Context
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface StreamsVideoViewDelegate {
    fun setStreamsVideoView(
        context: Context,
        participants: Flow<CallParticipants>,
        scope: CoroutineScope
    ) {
        participants
            .map { it.list }
            .mapParticipantsToVideos()
            .transform { videos -> videos.forEach { emit(it) } }
            .filterIsInstance<Input.Video>()
            .onEach { video ->
                if (video.view.value != null) return@onEach
                video.view.value = VideoStreamView(context.applicationContext)
            }
            .launchIn(scope)
    }

    private fun Flow<List<CallParticipant>>.mapParticipantsToVideos(): Flow<List<Input.Video?>> {
        return this.flatMapLatest { participants ->
            val participantVideos = mutableMapOf<String, List<Input.Video?>>()
            participants.map { participant ->
                participant.streams
                    .mapStreamsToVideos()
                    .map { Pair(participant.userId, it) }
            }
                .merge()
                .transform { (userId, videos) ->
                    participantVideos[userId] = videos
                    val values = participantVideos.values.toList()
                    if (values.size == participants.size) {
                        emit(values.flatten())
                    }
                }
        }
    }

    private fun Flow<List<Stream>>.mapStreamsToVideos(): Flow<List<Input.Video?>> {
        return this.flatMapLatest { streams ->
            val streamVideos = mutableMapOf<String, Input.Video?>()
            streams
                .map { stream ->
                    stream.video
                        .map { Pair(stream.id, it) }
                }
                .merge()
                .transform { (streamId, video) ->
                    streamVideos[streamId] = video
                    val values = streamVideos.values.toList()
                    if (values.size == streams.size) {
                        emit(values)
                    }
                }
        }
    }
}