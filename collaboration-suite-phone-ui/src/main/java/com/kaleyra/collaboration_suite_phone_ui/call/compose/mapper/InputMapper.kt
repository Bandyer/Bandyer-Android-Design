package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import android.os.Build
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.UsbCameraUtils
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasUsbCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UsbCameraMessage
import kotlinx.coroutines.flow.*

internal object InputMapper {

    fun Flow<Call>.toMutedMessage(): Flow<MutedMessage> =
        this.toAudio()
            .flatMapLatest { it.events }
            .filterIsInstance<Input.Audio.Event.Request.Mute>()
            .map { event -> event.producer.displayName.first() }
            .map { MutedMessage(it) }

    fun Flow<Call>.isAudioOnly(): Flow<Boolean> =
        this.map { it.extras }
            .map { !it.preferredType.hasVideo() }
            .distinctUntilChanged()

    fun Flow<Call>.isAudioVideo(): Flow<Boolean> =
        this.map { it.extras }
            .map { it.preferredType.isVideoEnabled() }
            .distinctUntilChanged()

    fun Flow<Call>.hasAudio(): Flow<Boolean> =
        this.map { it.extras }
            .map { it.preferredType.hasAudio() }
            .distinctUntilChanged()

    fun Flow<Call>.isMyCameraEnabled(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.video.firstOrNull { it is Input.Video.Camera } != null
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

    private fun Flow<Call>.toMe(): Flow<CallParticipant.Me> =
        this.flatMapLatest { it.participants }
            .map { it.me }
            .distinctUntilChanged()

    private fun Flow<Call>.toAudio(): Flow<Input.Audio> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.audio.firstOrNull { it != null } != null
                }
            }
            .filterNotNull()
            .flatMapLatest { it.audio }
            .filterNotNull()
            .distinctUntilChanged()
}