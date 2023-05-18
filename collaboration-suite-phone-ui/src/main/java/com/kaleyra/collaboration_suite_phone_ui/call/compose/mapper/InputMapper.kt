package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import kotlinx.coroutines.flow.*

internal object InputMapper {

    fun Flow<Call>.hasBeenMutedBy(): Flow<String?> =
        this.toAudio()
            .flatMapLatest { it.events }
            .filterIsInstance<Input.Audio.Event.Request.Mute>()
            .map { event -> event.producer.displayName.first() }

    fun Flow<Call>.hasVideo(): Flow<Boolean> =
        this.map { it.extras }
            .map { it.preferredType.hasVideo() }

    fun Flow<Call>.hasAudio(): Flow<Boolean> =
        this.map { it.extras }
            .map { it.preferredType.hasAudio() }

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

    fun Flow<Call>.isMyMicEnabled(): Flow<Boolean> =
        this.toAudio().flatMapLatest { it.enabled }

    fun Flow<Call>.isSharingScreen(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.any { stream -> stream.id == SCREEN_SHARE_STREAM_ID } }

    private fun Flow<Call>.toMe(): Flow<CallParticipant.Me> =
        flatMapLatest { it.participants }.map { it.me }

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
}