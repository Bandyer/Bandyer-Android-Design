package com.kaleyra.collaboration_suite_core_ui.utils

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input

internal object CallUtils {

    fun hasUsbInput(call: Call): Boolean {
        val inputs = call.inputs.availableInputs.value
        return inputs.any { it is Input.Video.Camera.Usb }
    }

    fun isMyCameraEnabled(call: Call): Boolean {
        val me = call.participants.value.me
        val videos = me.streams.value.map { it.video.value }
        val video = videos. firstOrNull { it is Input.Video.Camera.Internal || it is Input.Video.Camera.Usb }
        return video?.enabled?.value ?: false
    }

    fun hasMyCameraFrontLenses(call: Call): Boolean {
        val me = call.participants.value.me
        val streams = me.streams.value
        val video = streams.map { it.video.value }.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull()
        return video?.currentLens?.value?.isRear == false
    }

    fun isMyScreenShareEnabled(call: Call): Boolean {
        val me = call.participants.value.me
        val streams = me.streams.value
        return streams.firstOrNull { it.video.value is Input.Video.Screen.My || it.video.value is Input.Video.Application } != null
    }

    fun isNotConnected(call: Call): Boolean = call.state.value !is Call.State.Connected

    fun isIncoming(call: Call) =
        call.state.value is Call.State.Disconnected && call.participants.value.let { it.creator() != it.me && it.creator() != null }

    fun hasUsersWithCameraEnabled(call: Call): Boolean {
        val participants = call.participants.value.list
        val streams = participants.map { it.streams.value }.flatten()
        val videos = streams.map { it.video.value }
        return videos.any { it != null && it.enabled.value && it is Input.Video.Camera }
    }

    fun getMyInternalCamera(call: Call) = call.inputs.availableInputs.value.firstOrNull { it is Input.Video.Camera.Internal }
}