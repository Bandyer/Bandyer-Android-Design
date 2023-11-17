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

package com.kaleyra.video_sdk.call.audiooutput.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class AudioDeviceUi(open val id: String) {

    data class Bluetooth(override val id: String, val name: String?, val connectionState: BluetoothDeviceState, val batteryLevel: Int?) : AudioDeviceUi(id = id)

    object LoudSpeaker : AudioDeviceUi(id = LoudSpeaker::class.java.name)

    object EarPiece : AudioDeviceUi(id = EarPiece::class.java.name)

    object WiredHeadset : AudioDeviceUi(id = WiredHeadset::class.java.name)

    object Muted : AudioDeviceUi(id = Muted::class.java.name)
}