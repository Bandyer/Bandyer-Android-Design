package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import kotlinx.coroutines.flow.*

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
}