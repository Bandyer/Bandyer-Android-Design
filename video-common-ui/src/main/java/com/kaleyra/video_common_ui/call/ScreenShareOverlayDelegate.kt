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

import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.overlay.AppViewOverlay
import com.kaleyra.video_common_ui.overlay.StatusBarOverlayView
import com.kaleyra.video_common_ui.overlay.ViewOverlayAttacher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

internal interface ScreenShareOverlayDelegate {

    fun syncScreenShareOverlay(context: Context, call: Call, scope: CoroutineScope) {
        var deviceScreenShareOverlay: AppViewOverlay? = null
        var appScreenShareOverlay: AppViewOverlay? = null

        isDeviceScreenShareEnabled(call)
            .distinctUntilChanged()
            .onEach {
                if (it) {
                    deviceScreenShareOverlay = AppViewOverlay(StatusBarOverlayView(context), ViewOverlayAttacher.OverlayType.GLOBAL)
                    deviceScreenShareOverlay!!.show(context)
                } else {
                    deviceScreenShareOverlay?.hide()
                    deviceScreenShareOverlay = null
                }
            }.onCompletion {
                deviceScreenShareOverlay?.hide()
                deviceScreenShareOverlay = null
            }.launchIn(scope)

        isApplicationScreenShareEnabled(call)
            .distinctUntilChanged()
            .onEach {
                if (it) {
                    appScreenShareOverlay = AppViewOverlay(StatusBarOverlayView(context), ViewOverlayAttacher.OverlayType.CURRENT_APPLICATION)
                    appScreenShareOverlay!!.show(context)
                } else {
                    appScreenShareOverlay?.hide()
                    appScreenShareOverlay = null
                }
            }.onCompletion {
                appScreenShareOverlay?.hide()
                appScreenShareOverlay = null
            }.launchIn(scope)
    }

    fun isDeviceScreenShareEnabled(call: Call): Flow<Boolean> =
        call.inputs
            .availableInputs
            .mapLatest { it.filterIsInstance<Input.Video.Screen.My>().firstOrNull() }
            .flatMapLatest { it?.enabled ?: flowOf(false) }


    fun isApplicationScreenShareEnabled(call: Call): Flow<Boolean> =
        call.inputs
            .availableInputs
            .mapLatest { it.filterIsInstance<Input.Video.Application>().firstOrNull() }
            .flatMapLatest { it?.enabled ?: flowOf(false) }
}