package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Effect
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object VirtualBackgroundMapper {

    fun Flow<CallUI>.toCurrentVirtualBackgroundUi(): Flow<VirtualBackgroundUi> {
        return this.toCurrentCameraVideoEffect()
            .map { it.mapToVirtualBackgroundUi() }
            .filterNotNull()
    }

    fun Flow<CallUI>.toVirtualBackgroundsUi(): Flow<List<VirtualBackgroundUi>> {
        val effectsFlow = this.map { it.effects }
        val availableFlow = effectsFlow.flatMapLatest { it.available }
        val preselectedFlow = effectsFlow.flatMapLatest { it.preselected }
        return combine(availableFlow, preselectedFlow) { available, preselected ->
            val blur = available.firstOrNull { it is Effect.Video.Background.Blur }?.mapToVirtualBackgroundUi()
            val image = preselected.takeIf { it is Effect.Video.Background.Image }?.mapToVirtualBackgroundUi()
            listOfNotNull(VirtualBackgroundUi.None, blur, image)
        }
    }

    fun Flow<CallUI>.hasVirtualBackground(): Flow<Boolean> {
        val preselectedFlow= this.flatMapLatest { it.effects.preselected }
        val availableFlow = this.flatMapLatest { it.effects.available }
        return combine(preselectedFlow, availableFlow) { preselectedEffect, availableEffect ->
            preselectedEffect != Effect.Video.None && availableEffect.isNotEmpty()
        }
    }

    fun Flow<CallUI>.isVirtualBackgroundEnabled(): Flow<Boolean> {
        return this.toCurrentCameraVideoEffect().map { it != Effect.Video.None }
    }

    private fun Flow<CallUI>.toCurrentCameraVideoEffect(): Flow<Effect> {
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
    }

    private fun Effect.mapToVirtualBackgroundUi(): VirtualBackgroundUi? {
        return when (this) {
            is Effect.Video.Background.Blur -> VirtualBackgroundUi.Blur(id = id)
            is Effect.Video.Background.Image -> VirtualBackgroundUi.Image(id = id)
            is Effect.Video.None -> VirtualBackgroundUi.None
            else -> null
        }
    }
}