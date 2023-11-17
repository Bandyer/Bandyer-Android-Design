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

import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

val mockCallActions = ImmutableList(
    listOf(
        CallAction.Microphone(isToggled = false, isEnabled = true),
        CallAction.Camera(isToggled = false, isEnabled = false),
        CallAction.SwitchCamera(true),
        CallAction.HangUp(true),
        CallAction.Chat(true),
        CallAction.Whiteboard(true),
        CallAction.Audio(
            true,
            AudioDeviceUi.Bluetooth(
                id = "id",
                name = null,
                connectionState = BluetoothDeviceState.Connected,
                batteryLevel = null
            )
        ),
        CallAction.FileShare(true),
        CallAction.ScreenShare(true),
        CallAction.VirtualBackground(true)
    )
)