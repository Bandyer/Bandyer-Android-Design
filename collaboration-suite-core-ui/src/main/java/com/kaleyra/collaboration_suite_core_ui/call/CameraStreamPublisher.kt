package com.kaleyra.collaboration_suite_core_ui.call

import com.kaleyra.collaboration_suite.conference.Call

interface CameraStreamPublisher {

    companion object {
        const val CAMERA_STREAM_ID = "camera"
    }

    /**
     * Publish my stream
     *
     * @param call The call
     */
    fun addCameraStream(call: Call) {
        val me = call.participants.value.me ?: return
        if (me.streams.value.firstOrNull { it.id == CAMERA_STREAM_ID } != null) return
        me.addStream(CAMERA_STREAM_ID).let {
            it.audio.value = null
            it.video.value = null
        }
    }
}