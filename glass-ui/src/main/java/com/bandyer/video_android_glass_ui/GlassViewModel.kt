package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        GlassViewModel(ProvidersHolder.callProvider!!) as T
}

internal class GlassViewModel(callLogicProvider: CallLogicProvider) : ViewModel() {
    val call: Flow<Call> = callLogicProvider.call

    val participants: Flow<CallParticipants> = call.flatMapConcat { it.participants }

    val streams: Flow<List<StreamParticipant>> = MutableSharedFlow<List<StreamParticipant>>(replay = 1, extraBufferCapacity = 1)
            .apply {
                val jobs = mutableMapOf<String, Job>()
                participants
                    .onEach { participants ->
                        val streams = mutableListOf<StreamParticipant>()
                        participants.others.plus(participants.me).forEach { participant ->
                            jobs[participant.id]?.cancel()
                            jobs[participant.id] = participant.streams.onEach {
                                streams.removeIf { stream -> stream.participant == participant }
                                streams +=
                                    if(it.none { stream -> stream.state !is Stream.State.Closed }) listOf(StreamParticipant(participant, participant == participants.me, null))
                                    else it.map { stream -> StreamParticipant(participant, participant == participants.me, stream) }
                                emit(streams)
                            }.launchIn(viewModelScope)
                        }
                    }.launchIn(viewModelScope)
            }
}

internal data class StreamParticipant(
    val participant: CallParticipant,
    val isMyStream: Boolean,
    val stream: Stream?
)

