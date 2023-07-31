package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.UsbCameraUtils
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.failedAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toMe
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.doIHaveStreams
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UsbCameraMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object InputMapper {

    fun Flow<Call>.isVideoIncoming(): Flow<Boolean> =
        combine(isAudioVideo(), doIHaveStreams()) { isVideoEnabled, doIHaveStreams ->
            isVideoEnabled && doIHaveStreams
        }.distinctUntilChanged()

    fun Flow<Call>.toAudioConnectionFailureMessage(): Flow<AudioConnectionFailureMessage> =
        this.flatMapLatest { it.failedAudioOutputDevice }
            .map {
                if (it.isInSystemCall) AudioConnectionFailureMessage.InSystemCall
                else AudioConnectionFailureMessage.Generic
            }

    fun Flow<Call>.toMutedMessage(): Flow<MutedMessage> =
        this.toAudio()
            .flatMapLatest { it.events }
            .filterIsInstance<Input.Audio.Event.Request.Mute>()
            .map { event -> event.producer.combinedDisplayName.first() }
            .map { MutedMessage(it) }

    fun Flow<Call>.isAudioOnly(): Flow<Boolean> =
        this.map { it.extras }
            .flatMapLatest { it.preferredType }
            .map { !it.hasVideo() }
            .distinctUntilChanged()

    fun Flow<Call>.isAudioVideo(): Flow<Boolean> =
        this.map { it.extras }
            .flatMapLatest { it.preferredType }
            .map { it.isVideoEnabled() }
            .distinctUntilChanged()

    fun Flow<Call>.hasAudio(): Flow<Boolean> =
        this.map { it.extras }
            .flatMapLatest { it.preferredType }
            .map { it.hasAudio() }
            .distinctUntilChanged()

    fun Flow<Call>.isMyCameraEnabled(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.video.firstOrNull() is Input.Video.Camera
                }
            }
            .filterNotNull()
            .flatMapLatest { it.video }
            .filterNotNull()
            .flatMapLatest { it.enabled }
            .distinctUntilChanged()

    fun Flow<Call>.isMyMicEnabled(): Flow<Boolean> =
        this.toAudio()
            .flatMapLatest { it.enabled }
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
                    usbCamera != null -> UsbCameraMessage.NotSupported
                    else -> UsbCameraMessage.Disconnected
                }
            }

    private fun Flow<Call>.toAudio(): Flow<Input.Audio> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.audio.filterNotNull().firstOrNull() != null
                }
            }
            .filterNotNull()
            .flatMapLatest { it.audio }
            .filterNotNull()
            .distinctUntilChanged()
}