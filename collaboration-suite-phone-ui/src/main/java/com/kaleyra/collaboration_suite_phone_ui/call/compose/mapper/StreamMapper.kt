package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VideoMapper.mapToVideoUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal object StreamMapper {
    fun Flow<Call>.toStreamsUi(): Flow<List<StreamUi>> {
        return flatMapLatest { it.participants }.flatMapLatest { participants ->
            val map = mutableMapOf<String, List<StreamUi>>()
            val participantsList = participants.list

            if (participantsList.isEmpty()) flowOf(listOf())
            else participantsList
                .map { participant ->
                    // TODO add the call participant state check?
//                combine(participant.streams, participant.state) { streams, state ->
//                    if (state == CallParticipant.State.InCall) streams
//                    else listOf()
//                }
                    participant.streams
                        .mapToStreamsUi(participant.displayName, participant.displayImage)
                        .map {
                            Pair(participant.userId, it)
                        }
                }
                .merge()
                .transform { (userId, streams) ->
                    map[userId] = streams
                    val values = map.values.toList()
                    if (values.size == participants.list.size) {
                        emit(values.flatten())
                    }
                }
        }
    }

    fun Flow<Call>.toMyStreamsUi(): Flow<List<StreamUi>> {
        return flatMapLatest { it.participants }
            .map { it.me }
            .flatMapLatest { me ->
                me.streams.mapToStreamsUi(me.displayName, me.displayImage)
            }
    }

    fun Flow<List<Stream>>.mapToStreamsUi(
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
                    val values = map.values.toList()
                    if (values.size == streams.size) {
                        emit(values)
                    }
                }
        }
    }
    
    fun Flow<Call>.doAnyOfMyStreamsIsLive(): Flow<Boolean> {
        return this.flatMapLatest { it.participants }
            .map { it.me }
            .flatMapLatest { it.streams }
            .flatMapLatest { streams ->
                val map = mutableMapOf<String, Boolean>()
                if (streams.isEmpty()) flowOf(false)
                else streams
                    .map { stream ->
                        stream.isMyStreamLive().map { Pair(stream.id, it) }
                    }
                    .merge()
                    .transform { (id, isLive) ->
                        map[id] = isLive
                        val values = map.values.toList()
                        if (values.size == streams.size) {
                            emit(values.any { it })
                        }
                    }
            }
    }


}