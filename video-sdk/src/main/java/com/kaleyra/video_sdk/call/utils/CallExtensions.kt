package com.kaleyra.video_sdk.call.utils

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamPublisher

internal object CallExtensions {

    fun Call.toMyCameraStream(): Stream.Mutable? {
        val me = participants.value.me ?: return null
        val streams = me.streams.value
        return streams.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID }
    }
}