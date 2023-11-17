/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Effect
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

internal object VirtualBackgroundMapper {

    fun Flow<CallUI>.toCurrentVirtualBackgroundUi(): Flow<VirtualBackgroundUi> =
        this.toCurrentCameraVideoEffect()
            .map { it.mapToVirtualBackgroundUi() }
            .filterNotNull()
            .distinctUntilChanged()

    fun Flow<CallUI>.toVirtualBackgroundsUi(): Flow<List<VirtualBackgroundUi>> {
        val effectsFlow = this.map { it.effects }
        val availableFlow = effectsFlow.flatMapLatest { it.available }
        val preselectedFlow = effectsFlow.flatMapLatest { it.preselected }
        return combine(availableFlow, preselectedFlow) { available, preselected ->
            val blur = available.firstOrNull { it is Effect.Video.Background.Blur }?.mapToVirtualBackgroundUi()
            val image = preselected.takeIf { it is Effect.Video.Background.Image }?.mapToVirtualBackgroundUi()
            listOfNotNull(VirtualBackgroundUi.None, blur, image)
        }.distinctUntilChanged()
    }

    fun Flow<CallUI>.hasVirtualBackground(): Flow<Boolean> {
        val preselectedFlow= this.flatMapLatest { it.effects.preselected }
        val availableFlow = this.flatMapLatest { it.effects.available }
        return combine(preselectedFlow, availableFlow) { preselectedEffect, availableEffect ->
            preselectedEffect != Effect.Video.None && availableEffect.isNotEmpty()
        }.distinctUntilChanged()
    }

    fun Flow<CallUI>.isVirtualBackgroundEnabled(): Flow<Boolean> =
        this.toCurrentCameraVideoEffect()
            .map { it != Effect.Video.None }
            .distinctUntilChanged()

    private fun Flow<CallUI>.toCurrentCameraVideoEffect(): Flow<Effect> =
        this.flatMapLatest { it.participants }
            .mapNotNull { it.me }
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID }
            }
            .filterNotNull()
            .flatMapLatest { it.video }
            .filterNotNull()
            .flatMapLatest { it.currentEffect }
            .distinctUntilChanged()

    private fun Effect.mapToVirtualBackgroundUi(): VirtualBackgroundUi? {
        return when (this) {
            is Effect.Video.Background.Blur -> VirtualBackgroundUi.Blur(id = id)
            is Effect.Video.Background.Image -> VirtualBackgroundUi.Image(id = id)
            is Effect.Video.None -> VirtualBackgroundUi.None
            else -> null
        }
    }
}