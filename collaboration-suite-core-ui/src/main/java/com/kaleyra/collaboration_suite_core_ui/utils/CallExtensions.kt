package com.kaleyra.collaboration_suite_core_ui.utils

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.CallParticipants
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isDND
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

internal object CallExtensions {

    fun Call.hasUsbInput(): Boolean {
        val inputs = inputs.availableInputs.value
        return inputs.any { it is Input.Video.Camera.Usb }
    }

    fun Call.isMyInternalCameraEnabled(): Boolean {
        val me = participants.value.me ?: return false
        val videos = me.streams.value.map { it.video.value }
        val video = videos.firstOrNull { it is Input.Video.Camera.Internal }
        return video?.enabled?.value ?: false
    }

    fun Call.isMyInternalCameraUsingFrontLens(): Boolean {
        val me = participants.value.me ?: return false
        val streams = me.streams.value
        val video = streams.map { it.video.value }.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull()
        return video?.currentLens?.value?.isRear == false
    }

    fun Call.isMyScreenShareEnabled(): Boolean {
        val me = participants.value.me ?: return false
        val streams = me.streams.value
        return streams.firstOrNull { it.video.value is Input.Video.Screen.My || it.video.value is Input.Video.Application } != null
    }

    fun Call.isNotConnected(): Boolean = state.value !is Call.State.Connected

    fun isIncoming(state: Call.State, participants: CallParticipants) =
        state is Call.State.Disconnected && participants.let { it.creator() != it.me && it.creator() != null }

    fun isOutgoing(state: Call.State, participants: CallParticipants) =
        state is Call.State.Connecting && participants.let { it.creator() == it.me }

    fun isOngoing(state: Call.State, participants: CallParticipants) =
        state is Call.State.Connecting || state is Call.State.Connected || participants.creator() == null

    fun Call.hasUsersWithCameraEnabled(): Boolean {
        val participants = participants.value.list
        val streams = participants.map { it.streams.value }.flatten()
        val videos = streams.map { it.video.value }
        return videos.any { it != null && it.enabled.value && it is Input.Video.Camera }
    }

    fun Call.getMyInternalCamera() = inputs.availableInputs.value.firstOrNull { it is Input.Video.Camera.Internal }

    fun CallUI.shouldShowAsActivity(): Boolean {
        val context = ContextRetainer.context
        return (!context.isDND() && !context.isSilent()) || isOutgoing(state.value, participants.value) || isLink
    }

    fun CallUI.showOnAppResumed(coroutineScope: CoroutineScope) {
        AppLifecycle
            .isInForeground
            .dropWhile { !it }
            .take(1)
            .onEach { show() }
            .launchIn(coroutineScope)
    }
}