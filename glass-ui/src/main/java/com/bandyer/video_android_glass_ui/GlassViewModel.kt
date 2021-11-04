package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bandyer.video_android_glass_ui.model.Call
import com.bandyer.video_android_glass_ui.model.CallParticipants
import com.bandyer.video_android_glass_ui.model.Input
import com.bandyer.video_android_glass_ui.model.Stream
import com.bandyer.video_android_glass_ui.model.internal.StreamParticipant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        GlassViewModel(ProvidersHolder.callProvider!!) as T
}

internal class GlassViewModel(private val callLogicProvider: CallLogicProvider) : ViewModel() {
    val call: Flow<Call> = callLogicProvider.call

    val callState: Flow<Call.State> = call.flatMapConcat { call -> call.state }

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
                                    if(it.none { stream -> stream.state !is Stream.State.Closed }) listOf(
                                        StreamParticipant(participant, participant == participants.me, null)
                                    )
                                    else it.map { stream -> StreamParticipant(participant, participant == participants.me, stream) }
                                emit(streams)
                            }.launchIn(viewModelScope)
                        }
                    }.launchIn(viewModelScope)
            }

    private val cameraStream: Flow<Stream?> =
        participants
            .map { it.me }
            .flatMapConcat { it.streams }
            .map { streams ->
                streams.firstOrNull { it.video.firstOrNull { video -> video?.source is Input.Video.Source.Camera.Internal } != null }
            }

    var isCameraEnabled = false
    val cameraEnabled: Flow<Boolean?> = cameraStream.filter { it != null }
        .flatMapConcat { it!!.video }
        .flatMapConcat { it!!.enabled }
        .onEach { isCameraEnabled = it == true }

    var isMicEnabled = false
    val micEnabled: Flow<Boolean?> = cameraStream.filter { it != null }
        .flatMapConcat { it!!.audio }
        .flatMapConcat { it!!.enabled }
        .onEach { isMicEnabled = it == true }

    fun enableCamera(enable: Boolean) = callLogicProvider.enableCamera(enable)

    fun enableMic(enable: Boolean) = callLogicProvider.enableMic(enable)

    fun answer() = callLogicProvider.answer()

    fun hangUp() = callLogicProvider.hangup()
}


