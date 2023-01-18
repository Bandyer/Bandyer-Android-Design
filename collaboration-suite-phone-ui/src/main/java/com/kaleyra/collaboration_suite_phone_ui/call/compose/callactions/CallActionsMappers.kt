package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions

import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import kotlinx.coroutines.flow.*

object CallActionsMappers {
    internal fun Flow<CallParticipant.Me>.isCameraEnabled(): Flow<Boolean> =
        this.flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.video.firstOrNull { it is Input.Video.Camera } != null
                }
            }
            .filter { it != null }
            .flatMapLatest { it!!.video }
            .filter { it != null }
            .flatMapLatest { it!!.enabled }

    internal fun Flow<CallParticipant.Me>.isAudioEnabled(): Flow<Boolean> =
        this.flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.audio.firstOrNull { it != null } != null
                }
            }
            .filter { it != null }
            .flatMapLatest { it!!.audio }
            .filter { it != null }
            .flatMapLatest { it!!.enabled }
}