package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Effect
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object VirtualBackgroundMapper {

    fun Flow<CallUI>.toCurrentVirtualBackground(): Flow<VirtualBackgroundUi> {
         return this
            .flatMapLatest { it.participants }
            .map { it.me }
            .flatMapLatest { it.streams }
             .map { streams ->
                 streams.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID }
             }
             .filterNotNull()
             .flatMapLatest { it.video }
             .filterNotNull()
             .flatMapLatest { it.currentEffect }
             .map { effect ->
                 effect.mapToVirtualBackgroundUi()
             }
             .filterNotNull()
    }

    fun Flow<CallUI>.toVirtualBackgroundsUi(): Flow<List<VirtualBackgroundUi>> {
        return this
            .map { it.effects }
            .flatMapLatest { it.available }
            .map { effects ->
                effects.mapNotNull { it.mapToVirtualBackgroundUi() }
            }
    }

    private fun Effect.mapToVirtualBackgroundUi(): VirtualBackgroundUi? {
        return when (this) {
            is Effect.Video.Background.Blur -> VirtualBackgroundUi.Blur
            is Effect.Video.Background.Image -> VirtualBackgroundUi.Image
            is Effect.Video.None -> VirtualBackgroundUi.None
            else -> null
        }
    }
}