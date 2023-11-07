package com.kaleyra.video_common_ui.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

object ParticipantMapper {

    fun Flow<Call>.toMe(): Flow<CallParticipant.Me> =
        this.flatMapLatest { it.participants }
            .mapNotNull { it.me }
            .distinctUntilChanged()

    fun Flow<Call>.toInCallParticipants(): Flow<List<CallParticipant>> =
        this.flatMapLatest { it.participants }
            .mapNotNull { participants -> participants.me?.let { Pair(it, participants.others) }}
            .flatMapLatest { (me, others) ->
                val inCallMap = mutableMapOf<String, CallParticipant>(me.userId to me)
                val notInCallMap = mutableMapOf<String, CallParticipant>()

                if (others.isEmpty()) flowOf<List<CallParticipant>>(listOf(me))
                else others
                    .map { participant ->
                        combine(participant.state, participant.streams) { state, streams ->
                            val isInCall = state == CallParticipant.State.InCall || streams.isNotEmpty()
                            Pair(participant, isInCall)
                        }
                    }
                    .merge()
                    .transform { (participant, isInCall) ->
                        if (isInCall) {
                            inCallMap[participant.userId] = participant
                            notInCallMap.remove(participant.userId)
                        } else {
                            notInCallMap[participant.userId] = participant
                            inCallMap.remove(participant.userId)
                        }
                        val values = (inCallMap.values + notInCallMap.values).toList()
                        if (values.size == others.size + 1) {
                            emit(inCallMap.values.toList())
                        }
                    }
            }
            .distinctUntilChanged()
}