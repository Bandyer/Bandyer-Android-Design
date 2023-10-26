package com.kaleyra.collaboration_suite_phone_ui.call.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Stream
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.ParticipantMapper.toMe
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.VideoMapper.mapToVideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal object StreamMapper {
    fun Flow<Call>.toStreamsUi(): Flow<List<StreamUi>> =
        this.flatMapLatest { it.participants }.flatMapLatest { participants ->
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
                        .mapToStreamsUi(participant.combinedDisplayName, participant.combinedDisplayImage)
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
        }.distinctUntilChanged()

    fun Flow<Call>.toMyStreamsUi(): Flow<List<StreamUi>> =
        this.flatMapLatest { it.participants }
            .mapNotNull { it.me }
            .flatMapLatest { me ->
                me.streams.mapToStreamsUi(me.combinedDisplayName, me.combinedDisplayImage)
            }
            .distinctUntilChanged()

    fun Flow<List<Stream>>.mapToStreamsUi(
        displayName: Flow<String?>,
        displayImage: Flow<Uri?>
    ): Flow<List<StreamUi>> =
         this.flatMapLatest { streams ->
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
        }.distinctUntilChanged()

    fun Flow<Call>.doIHaveStreams(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { it.isNotEmpty() }
            .distinctUntilChanged()

    fun Flow<List<StreamUi>>.hasAtLeastAVideoEnabled(): Flow<Boolean> =
        this.map { streams -> streams.map { it.video } }
            .map { video -> video.any { it?.isEnabled == true } }
            .distinctUntilChanged()

}