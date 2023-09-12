package com.kaleyra.collaboration_suite_phone_ui.call.extensions

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Stream
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher

internal object CallExtensions {

    fun Call.toMyCameraStream(): Stream.Mutable? {
        val me = participants.value.me
        val streams = me.streams.value
        return streams.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID }
    }
}