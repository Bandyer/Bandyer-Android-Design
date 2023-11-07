package com.kaleyra.video_common_ui.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

object StreamMapper {

    fun Flow<Call>.doAnyOfMyStreamsIsLive(): Flow<Boolean> =
        this.flatMapLatest { it.participants }
            .mapNotNull { it.me }
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
            .distinctUntilChanged()

    fun Flow<Call>.doOthersHaveStreams(): Flow<Boolean> =
        this.flatMapLatest { it.participants }
            .map { it.others }
            .flatMapLatest { participants ->
                val map = mutableMapOf<String, Boolean>()
                if (participants.isEmpty()) flowOf(false)
                else participants
                    .map { participant ->
                        participant.streams.map { Pair(participant, it.isNotEmpty()) }
                    }
                    .merge()
                    .transform { (participant, hasStreams) ->
                        map[participant.userId] = hasStreams
                        val values = map.values.toList()
                        if (values.size == participants.size) {
                            emit(values.any { it })
                        }
                    }
            }
            .distinctUntilChanged()

    fun Flow<Call>.amIAlone(): Flow<Boolean> =
        combine(
            doOthersHaveStreams(),
            doAnyOfMyStreamsIsLive()
        ) { doesOthersHaveStreams, doAnyOfMyStreamsIsLive ->
            !doesOthersHaveStreams || !doAnyOfMyStreamsIsLive
        }.distinctUntilChanged()


    private fun Stream.isMyStreamLive(): Flow<Boolean> =
        this.state.map { it is Stream.State.Live }

    fun Flow<Call>.amIWaitingOthers(): Flow<Boolean> =
        combine(
            flatMapLatest { it.state },
            amIAlone(),
            toInCallParticipants()
        ) { callState, amIAlone, inCallParticipants ->
            callState is Call.State.Connected && amIAlone && inCallParticipants.size == 1
        }.distinctUntilChanged()
}