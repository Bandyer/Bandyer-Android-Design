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

package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

interface CameraStreamPublisher {

    companion object {
        const val CAMERA_STREAM_ID = "camera"
    }

    /**
     * Publish my stream
     *
     * @param call The call
     */
    fun addCameraStream(call: Call, scope: CoroutineScope) {
        scope.launch {
            val me = call.participants.mapNotNull { it.me }.first()
            if (me.streams.value.firstOrNull { it.id == CAMERA_STREAM_ID } != null) return@launch
            me.addStream(CAMERA_STREAM_ID).let {
                it.audio.value = null
                it.video.value = null
            }
        }
    }
}