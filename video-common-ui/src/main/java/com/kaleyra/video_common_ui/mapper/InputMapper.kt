package com.kaleyra.video_common_ui.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toMe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

object InputMapper {

    inline fun <reified T:Input> Flow<Call>.isInputActive(): Flow<Boolean> =
        flatMapLatest { it.inputs.availableInputs }
            .map { inputs -> inputs.firstOrNull { it is T } }
            .flatMapLatest { it?.state ?: flowOf(null) }
            .map { it is Input.State.Active }
            .distinctUntilChanged()

    fun Flow<Call>.isDeviceScreenInputActive(): Flow<Boolean> = isInputActive<Input.Video.Screen>()

    fun Flow<Call>.isAppScreenInputActive(): Flow<Boolean> = isInputActive<Input.Video.Application>()

    fun Flow<Call>.isAnyScreenInputActive(): Flow<Boolean> =
        combine(isDeviceScreenInputActive(), isAppScreenInputActive()) { isDeviceScreenInputActive, isAppScreenInputActive ->
            isDeviceScreenInputActive || isAppScreenInputActive
    }

    fun Flow<Call>.toMuteEvents(): Flow<Input.Audio.Event.Request.Mute> =
        this.toAudio()
            .filterNotNull()
            .flatMapLatest { it.events }
            .filterIsInstance<Input.Audio.Event.Request.Mute>()
            .distinctUntilChanged()

    fun Flow<Call>.toAudio(): Flow<Input.Audio?> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.firstOrNull { stream -> stream.id == CameraStreamPublisher.CAMERA_STREAM_ID } }
            .flatMapLatest { it?.audio ?: flowOf(null) }

    fun Flow<Call>.hasScreenSharingInput(): Flow<Boolean> =
        this.flatMapLatest { it.inputs.availableInputs }.map { inputs -> inputs.any { it is Input.Video.Screen.My } }
}
