package com.kaleyra.collaboration_suite_core_ui.call

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Input
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface CameraStreamInputsDelegate {

    fun handleCameraStreamAudio(call: Call, coroutineScope: CoroutineScope) {
        call.inputs.availableInputs
            .map { it.filterIsInstance<Input.Audio>().firstOrNull() }
            .filterNotNull()
            .onEach { audio ->
                val me = call.participants.value.me ?: return@onEach
                val stream = me.streams.value.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID } ?: return@onEach
                stream.audio.value = audio
            }
            .launchIn(coroutineScope)
    }

    fun handleCameraStreamVideo(call: Call, coroutineScope: CoroutineScope) {
        call.inputs.availableInputs
            .map { inputs -> inputs.lastOrNull { it is Input.Video.Camera }}
            .filterIsInstance<Input.Video.My>()
            .combine(call.preferredType) { video, preferredType ->
                val hasVideo = preferredType.hasVideo()
                if (!hasVideo) return@combine
                val me = call.participants.value.me ?: return@combine
                val stream = me.streams.value.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID } ?: return@combine
                video.setQuality(Input.Video.Quality.Definition.HD)
                stream.video.value = video
            }.launchIn(coroutineScope)
    }

}