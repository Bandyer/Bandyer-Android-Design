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

import android.util.Rational
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.utils.MathUtils
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object CallUiStateMapper {

    fun Flow<CallUiState>.toPipAspectRatio(): Flow<Rational> =
        this.map { it.featuredStreams }
            .map { it.value.firstOrNull() }
            .filterNotNull()
            .distinctUntilChanged()
            .map { it.video?.view?.value as? VideoStreamView }
            .filterNotNull()
            .flatMapLatest { it.videoSize }
            .map {
                val gcd = MathUtils.findGreatestCommonDivisor(it.width, it.height)
                if (gcd != 0) Rational(it.width / gcd, it.height / gcd)
                else Rational.NaN
            }
            .distinctUntilChanged()

}