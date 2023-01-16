package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import kotlinx.coroutines.flow.*

internal fun Flow<CallParticipants>.reduceToStreamsUi(): Flow<List<StreamUi>> {
    return flatMapLatest { participants ->
        val map = mutableMapOf<String, List<StreamUi>>()
        val participantsList = participants.list

        if (participantsList.isEmpty()) flowOf(listOf())
        else participantsList
            .map { participant ->
                participant.streams
                    .mapToStreamsUi(participant.displayName, participant.displayImage)
                    .map {
                        Pair(participant.userId, it)
                    }
            }
            .merge()
            .transform { (userId, streamsFlow) ->
                map[userId] = streamsFlow
                val values = map.values.toList()
                if (values.size == participants.list.size) {
                    emit(values.flatten())
                }
            }
    }
}

internal fun Flow<List<Stream>>.mapToStreamsUi(
    displayName: Flow<String?>,
    displayImage: Flow<Uri?>
): Flow<List<StreamUi>> {
    return flatMapLatest { streams ->
        val map = mutableMapOf<String, StreamUi>()

        if (streams.isEmpty()) flowOf(listOf())
        else streams
            .map { stream ->
                val id = stream.id
                val video = stream.video.mapToVideoUi()

                combine(
                    video,
                    displayName,
                    displayImage
                ) { video, name, image ->
                    StreamUi(
                        id = id,
                        video = video,
                        username = name ?: "",
                        avatar = image?.let { ImmutableUri(it) }
                    )
                }
            }
            .merge()
            .transform { stream ->
                map[stream.id] = stream
                val result = map.values.toList()
                if (result.size == streams.size) {
                    emit(result)
                }
            }
    }
}

internal fun StateFlow<Input.Video?>.mapToVideoUi(): Flow<VideoUi?> {
    return flow {
        val initialValue = value?.let {
            VideoUi(it.id, it.view.value, it.enabled.value)
        }
        emit(initialValue)

        val flow = this@mapToVideoUi.filter { it != null }
        combine(
            flow.map { it!!.id },
            flow.flatMapLatest { it!!.view },
            flow.flatMapLatest { it!!.enabled }
        ) { id, view, enabled ->
            VideoUi(id, view, enabled)
        }.collect {
            emit(it)
        }
    }
}
