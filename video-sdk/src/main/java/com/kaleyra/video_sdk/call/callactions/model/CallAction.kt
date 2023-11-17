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

package com.kaleyra.video_sdk.call.callactions.model

import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi

@Stable
sealed interface CallAction {

    val isEnabled: Boolean

    sealed interface Toggleable : CallAction {
        val isToggled: Boolean
    }

    data class Camera(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    data class Microphone(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    data class ScreenShare(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    data class VirtualBackground(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    data class SwitchCamera(override val isEnabled: Boolean = true) : CallAction

    data class HangUp(override val isEnabled: Boolean = true) : CallAction

    data class Chat(override val isEnabled: Boolean = true) : CallAction

    data class Whiteboard(override val isEnabled: Boolean = true) : CallAction

    data class FileShare(override val isEnabled: Boolean = true) : CallAction

    data class Audio(override val isEnabled: Boolean = true, val device: AudioDeviceUi = AudioDeviceUi.Muted) :
        CallAction

}