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

package com.kaleyra.video_common_ui.utils

import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object InputsExtensions {

    fun Inputs.useBackCamera(coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        coroutineScope.launch {
            val input = availableInputs
                .map { inputs -> inputs.lastOrNull { it is Input.Video.Camera.Internal }}
                .filterIsInstance<Input.Video.Camera.Internal>()
                .first()

            if (input.currentLens.value.isRear) return@launch
            val newLens = input.lenses.firstOrNull { it.isRear } ?: return@launch
            input.setLens(newLens)
        }
    }
}