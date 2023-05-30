package com.kaleyra.collaboration_suite_core_ui.utils

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input

internal object CallExtensions {

    fun Call.hasUsbInput(): Boolean {
        val inputs = inputs.availableInputs.value
        return inputs.any { it is Input.Video.Camera.Usb }
    }

    fun Call.isMyInternalCameraEnabled(): Boolean {
        val me = participants.value.me
        val videos = me.streams.value.map { it.video.value }
        val video = videos.firstOrNull { it is Input.Video.Camera.Internal }
        return video?.enabled?.value ?: false
    }

    fun Call.isMyInternalCameraUsingFrontLens(): Boolean {
        val me = participants.value.me
        val streams = me.streams.value
        val video = streams.map { it.video.value }.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull()
        return video?.currentLens?.value?.isRear == false
    }

    fun Call.isMyScreenShareEnabled(): Boolean {
        val me = participants.value.me
        val streams = me.streams.value
        return streams.firstOrNull { it.video.value is Input.Video.Screen.My || it.video.value is Input.Video.Application } != null
    }

    fun Call.isNotConnected(): Boolean = state.value !is Call.State.Connected

    fun Call.isIncoming() =
        state.value is Call.State.Disconnected && participants.value.let { it.creator() != it.me && it.creator() != null }

    fun Call.isOutgoing() =
        state.value is Call.State.Connecting && participants.value.let { it.creator() == it.me }

    fun Call.isOngoing() =
        state.value is Call.State.Connecting || state.value is Call.State.Connected || participants.value.creator() == null

    fun Call.hasUsersWithCameraEnabled(): Boolean {
        val participants = participants.value.list
        val streams = participants.map { it.streams.value }.flatten()
        val videos = streams.map { it.video.value }
        return videos.any { it != null && it.enabled.value && it is Input.Video.Camera }
    }

    fun Call.getMyInternalCamera() = inputs.availableInputs.value.firstOrNull { it is Input.Video.Camera.Internal }
}