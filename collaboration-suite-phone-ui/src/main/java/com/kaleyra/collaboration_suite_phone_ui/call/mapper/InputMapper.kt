package com.kaleyra.collaboration_suite_phone_ui.call.mapper

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.UsbCameraUtils
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.failedAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.ParticipantMapper.toMe
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.doIHaveStreams
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UsbCameraMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal object InputMapper {

    fun Flow<Call>.isVideoIncoming(): Flow<Boolean> =
        combine(isAudioVideo(), doIHaveStreams()) { isVideoEnabled, doIHaveStreams ->
            isVideoEnabled && doIHaveStreams
        }.distinctUntilChanged()

    fun Flow<Call>.toAudioConnectionFailureMessage(): Flow<AudioConnectionFailureMessage> =
        this.flatMapLatest { it.failedAudioOutputDevice }
            .filterNotNull()
            .map {
                if (it.isInSystemCall) AudioConnectionFailureMessage.InSystemCall
                else AudioConnectionFailureMessage.Generic
            }

    fun Flow<Call>.toMutedMessage(): Flow<MutedMessage> =
        this.toAudio()
            .filterNotNull()
            .flatMapLatest { it.events }
            .filterIsInstance<Input.Audio.Event.Request.Mute>()
            .map { event -> event.producer.combinedDisplayName.first() }
            .map { MutedMessage(it) }

    fun Flow<Call>.isAudioOnly(): Flow<Boolean> =
        this.flatMapLatest { it.preferredType }
            .map { !it.hasVideo() }
            .distinctUntilChanged()

    fun Flow<Call>.isAudioVideo(): Flow<Boolean> =
        this.flatMapLatest { it.preferredType }
            .map { it.isVideoEnabled() }
            .distinctUntilChanged()

    fun Flow<Call>.hasAudio(): Flow<Boolean> =
        this.flatMapLatest { it.preferredType }
            .map { it.hasAudio() }
            .distinctUntilChanged()

    fun Flow<Call>.isMyCameraEnabled(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.firstOrNull { stream -> stream.id == CAMERA_STREAM_ID } }
            .flatMapLatest { it?.video ?: flowOf(null) }
            .flatMapLatest { it?.enabled ?: flowOf(false) }
            .distinctUntilChanged()

    fun Flow<Call>.isMyMicEnabled(): Flow<Boolean> =
        this.toAudio()
            .flatMapLatest { it?.enabled ?: flowOf(false) }
            .distinctUntilChanged()

    fun Flow<Call>.isSharingScreen(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.any { stream -> stream.id == SCREEN_SHARE_STREAM_ID } }
            .distinctUntilChanged()

    fun Flow<Call>.hasUsbCamera(): Flow<Boolean> =
        this.flatMapLatest { it.inputs.availableInputs }
            .map { inputs -> inputs.firstOrNull { it is Input.Video.Camera.Usb } != null }
            .distinctUntilChanged()

    fun Flow<Call>.toUsbCameraMessage(): Flow<UsbCameraMessage> =
        this.flatMapLatest { it.inputs.availableInputs }
            .map { inputs ->
                val usbCamera = inputs.firstOrNull { it is Input.Video.Camera.Usb }
                when {
                    usbCamera != null && UsbCameraUtils.isSupported() -> UsbCameraMessage.Connected((usbCamera as Input.Video.Camera.Usb).name)
                    usbCamera != null                                 -> UsbCameraMessage.NotSupported
                    else                                              -> UsbCameraMessage.Disconnected
                }
            }

    private fun Flow<Call>.toAudio(): Flow<Input.Audio?> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.firstOrNull { stream -> stream.id == CAMERA_STREAM_ID } }
            .flatMapLatest { it?.audio ?: flowOf(null) }

}