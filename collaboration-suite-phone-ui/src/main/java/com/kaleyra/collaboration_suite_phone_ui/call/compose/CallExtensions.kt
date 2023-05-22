package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID

internal object CallExtensions {

    suspend fun Call.startMicrophone(context: FragmentActivity) {
        val result = inputs.request(context, Inputs.Type.Microphone)
        val audio = result.getOrNull<Input.Audio>() ?: return
        toMyCameraStream()?.audio?.value = audio
    }

    suspend fun Call.startCamera(context: FragmentActivity) {
        val result = inputs.request(context, Inputs.Type.Camera.Internal)
        val video = result.getOrNull<Input.Video.Camera.Internal>() ?: return
        toMyCameraStream()?.video?.value = video
    }

    fun Call.toMyCameraStream(): Stream.Mutable? {
        val me = participants.value.me
        val streams = me.streams.value
        return streams.firstOrNull { it.id == CAMERA_STREAM_ID }
    }
}