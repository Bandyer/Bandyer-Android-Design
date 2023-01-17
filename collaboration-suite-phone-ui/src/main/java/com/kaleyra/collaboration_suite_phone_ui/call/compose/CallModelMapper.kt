package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import kotlinx.coroutines.flow.*

internal fun Flow<Call>.toCallStateUi(): Flow<CallState> {
    return combine(
        flatMapLatest { it.state },
        flatMapLatest { it.participants }
    ) { state, participants ->
        when {
            state is Call.State.Connected -> CallState.Connected
            state is Call.State.Reconnecting -> CallState.Reconnecting
            state is Call.State.Connecting && participants.me == participants.creator() -> CallState.Dialing
            state is Call.State.Connecting -> CallState.Connecting
            state is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> CallState.Disconnected.Ended.AnsweredOnAnotherDevice
            state is Call.State.Disconnected.Ended.Declined -> CallState.Disconnected.Ended.Declined
            state is Call.State.Disconnected.Ended.LineBusy -> CallState.Disconnected.Ended.LineBusy
            state is Call.State.Disconnected.Ended.Timeout -> CallState.Disconnected.Ended.Timeout
            state is Call.State.Disconnected.Ended.Error.Server -> CallState.Disconnected.Ended.Error.Server
            state is Call.State.Disconnected.Ended.Error.Unknown -> CallState.Disconnected.Ended.Error.Unknown
            state is Call.State.Disconnected.Ended.HungUp -> CallState.Disconnected.Ended.HangUp
            state is Call.State.Disconnected.Ended.Kicked -> CallState.Disconnected.Ended.Kicked(state.userId)
            state == Call.State.Disconnected.Ended.Error -> CallState.Disconnected.Ended.Error
            state == Call.State.Disconnected.Ended -> CallState.Disconnected.Ended
            state is Call.State.Disconnected && participants.me != participants.creator() -> CallState.Ringing
            else -> CallState.Disconnected
        }
    }
}

internal fun Flow<CallParticipants>.toOtherDisplayNames(): Flow<List<String>> {
    return flatMapLatest { participants ->
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

internal fun Flow<Call.Recording>.mapToRecordingUi(): Flow<Recording?> =
    map {
        when (it.type) {
            is Call.Recording.Type.OnConnect -> Recording.OnConnect
            is Call.Recording.Type.OnDemand -> Recording.OnDemand
            else -> null
        }
    }

internal fun Flow<Call>.isRecording(): Flow<Boolean> =
    flatMapLatest { it.extras.recording.state }.map { it == Call.Recording.State.Started }

internal fun Flow<CallParticipants>.isGroupCall(): Flow<Boolean> = map { it.others.size > 1 }

internal fun Flow<CallParticipants>.reduceToStreamsUi(): Flow<List<StreamUi>> {
    return flatMapLatest { participants ->
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
                val values = map.values.toList()
                if (values.size == streams.size) {
                    emit(values)
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
