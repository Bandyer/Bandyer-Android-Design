package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal object ParticipantMapper {

    fun Flow<Call>.isGroupCall(): Flow<Boolean> =
        flatMapLatest { it.participants }.map { it.others.size > 1 }

    fun Flow<Call>.toOtherDisplayNames(): Flow<List<String>> {
        return flatMapLatest { it.participants }
            .flatMapLatest { participants ->
                val others = participants.others
                val map = mutableMapOf<String, String?>()

                if (others.isEmpty()) flowOf(listOf())
                else others
                    .map { participant ->
                        participant.displayName.map { displayName ->
                            Pair(participant.userId, displayName)
                        }
                    }
                    .merge()
                    .transform { (userId, displayName) ->
                        map[userId] = displayName
                        val values = map.values.toList().filterNotNull()
                        if (values.size == others.size) {
                            emit(values)
                        }
                    }
            }
    }

    fun Flow<Call>.toOtherDisplayImages(): Flow<List<Uri>> {
        return flatMapLatest { it.participants }
            .flatMapLatest { participants ->
                val others = participants.others
                val map = mutableMapOf<String, Uri?>()

                if (others.isEmpty()) flowOf(listOf())
                else others
                    .map { participant ->
                        participant.displayImage.map { displayName ->
                            Pair(participant.userId, displayName)
                        }
                    }
                    .merge()
                    .transform { (userId, displayImage) ->
                        map[userId] = displayImage
                        val values = map.values.toList().filterNotNull()
                        if (values.size == others.size) {
                            emit(values)
                        }
                    }
            }
    }

    fun Flow<Call>.toInCallParticipants(): Flow<List<CallParticipant>> {
        return flatMapLatest { it.participants }
            .map { Pair(it.me, it.others) }
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
    }
}