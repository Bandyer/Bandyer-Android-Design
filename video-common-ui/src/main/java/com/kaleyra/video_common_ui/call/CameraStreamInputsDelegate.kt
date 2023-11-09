package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

interface CameraStreamInputsDelegate {

    fun handleCameraStreamAudio(call: Call, coroutineScope: CoroutineScope) {
        combine(
            call.inputs.availableInputs
                .map { it.filterIsInstance<Input.Audio>().firstOrNull() }
                .filterNotNull(),
            call.participants.mapNotNull { it.me }
        ) { audio, me ->
            val stream = me.streams.value.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID } ?: return@combine
            stream.audio.value = audio
        }.launchIn(coroutineScope)
    }

    fun handleCameraStreamVideo(call: Call, coroutineScope: CoroutineScope) {
        combine(
            call.inputs.availableInputs
                .map { inputs -> inputs.lastOrNull { it is Input.Video.Camera }}
                .filterIsInstance<Input.Video.My>(),
            call.preferredType,
            call.participants.mapNotNull { it.me }
        ) { video, preferredType, me ->
            val hasVideo = preferredType.hasVideo()
            if (!hasVideo) return@combine
            val stream = me.streams.value.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID } ?: return@combine
            video.setQuality(Input.Video.Quality.Definition.HD)
            stream.video.value = video
        }.launchIn(coroutineScope)
    }

}