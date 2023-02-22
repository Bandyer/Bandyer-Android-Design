package com.kaleyra.collaboration_suite_core_ui.call

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface CameraStreamInputsDelegate {
    fun updateCameraStreamOnInputs(call: Call, coroutineScope: CoroutineScope) {
        val hasVideo = call.extras.preferredType.hasVideo()
        val availableInputs = call.inputs.availableInputs

        availableInputs
            .map { inputs -> inputs.filter { it is Input.Video.Camera || it is Input.Audio } }
            .onEach { inputs ->
                if (inputs.isEmpty()) return@onEach

                val videoInput = inputs.lastOrNull { it is Input.Video.My } as? Input.Video.My
                val audioInput = inputs.firstOrNull { it is Input.Audio } as? Input.Audio

                videoInput?.setQuality(Input.Video.Quality.Definition.HD)

                val me = call.participants.value.me
                me.streams.value.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID }?.let {
                    it.audio.value = audioInput
                    if (hasVideo) it.video.value = videoInput
                }
            }.launchIn(coroutineScope)
    }
}