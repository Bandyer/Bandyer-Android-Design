package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

interface CameraStreamPublisher {

    companion object {
        const val CAMERA_STREAM_ID = "camera"
    }

    /**
     * Publish my stream
     *
     * @param call The call
     */
    fun addCameraStream(call: Call, scope: CoroutineScope) {
        scope.launch {
            val me = call.participants.mapNotNull { it.me }.first()
            if (me.streams.value.firstOrNull { it.id == CAMERA_STREAM_ID } != null) return@launch
            me.addStream(CAMERA_STREAM_ID).let {
                it.audio.value = null
                it.video.value = null
            }
        }
    }
}