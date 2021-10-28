package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        GlassViewModel(ProvidersHolder.callProvider!!) as T
}

internal class GlassViewModel(callLogicProvider: CallLogicProvider) : ViewModel() {

    val call: Flow<Call> = callLogicProvider.call

    val callState: Flow<Call.State> = call.flatMapConcat { call -> call.state }

    val participants: Flow<CallParticipants> = call.flatMapConcat { call -> call.participants }

//    fun observeStreamsAudio(): StateFlow<Map<String, Boolean>> {
//        val streamsAudio: MutableStateFlow<Map<String, Boolean>> = MutableStateFlow(emptyMap())
//        participants.onEach { participants ->
//            participants.others
//                .plus(participants.me)
//                .map { participant -> participant.streams }
//                .merge()
//                .map { streams ->
//                    streams.map { stream ->
//                        stream.id to stream.audio.flatMapConcat { audio ->
//                            audio?.enabled ?: flow { emit(false) }
//                        }
//                    }.toMap()
//                }
//        }.launchIn(viewModelScope)
//        return streamsAudio
//    }
}

internal data class ParticipantStreamInfo(
    val isMyStream: Boolean,
    val username: String,
    val avatarUrl: String?,
    val stream: Stream
)
