package com.kaleyra.collaboration_suite_core_ui.call

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import kotlinx.coroutines.CoroutineScope
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
                val me = call.participants.value.me
                val stream = me.streams.value.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID } ?: return@onEach
                stream.audio.value = audio
            }
            .launchIn(coroutineScope)
    }

    fun handleCameraStreamVideo(call: Call, coroutineScope: CoroutineScope) {
        val hasVideo = call.extras.preferredType.hasVideo()
        call.inputs.availableInputs
            .map { inputs -> inputs.lastOrNull { it is Input.Video.Camera.Internal || it is Input.Video.Camera.Usb } as? Input.Video.My }
            .filterNotNull()
            .onEach { video ->
                video.setQuality(Input.Video.Quality.Definition.HD)
                val me = call.participants.value.me
                val stream = me.streams.value.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID } ?: return@onEach
                if (hasVideo) stream.video.value = video
            }.launchIn(coroutineScope)
    }

}