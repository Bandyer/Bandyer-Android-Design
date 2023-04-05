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

package com.kaleyra.collaboration_suite_core_ui.call

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_core_ui.model.Volume
import com.kaleyra.collaboration_suite_utils.audio.CallAudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * CallController. It implements the CallUIController methods.
 */
class CallController(private val call: SharedFlow<CallUI>, private val callAudioManager: CallAudioManager) : CallUIController {

    private val currentCall: Call get() = call.replayCache.first()

    private val _micPermission = MutableStateFlow(Permission(isAllowed = false, neverAskAgain = false))

    private val _camPermission = MutableStateFlow(Permission(isAllowed = false, neverAskAgain = false))

    override val micPermission: StateFlow<Permission> = _micPermission.asStateFlow()

    override val camPermission: StateFlow<Permission> = _camPermission.asStateFlow()

    override val volume: Volume get() = callAudioManager.let { Volume(it.currentVolume, it.minVolume, it.maxVolume) }

    override fun onRequestMicPermission(context: FragmentActivity) {
        context.lifecycleScope.launchWhenResumed {
            val inputRequest = currentCall.inputs.request(context, Inputs.Type.Microphone)

            _micPermission.value = Permission(
                inputRequest is Inputs.RequestResult.Success,
                inputRequest is Inputs.RequestResult.Error.PermissionDenied.Forever
            )

            val input = inputRequest.getOrNull<Input.Audio>() ?: return@launchWhenResumed

            input.state.filter { it is Input.State.Closed }.onEach {
                _micPermission.value = Permission(false, false)
            }.launchIn(this)

            currentCall.participants.value.me.streams.value.firstOrNull { it.id == CAMERA_STREAM_ID }?.audio?.value = input
        }
    }


    override fun onRequestCameraPermission(context: FragmentActivity) {
        context.lifecycleScope.launchWhenResumed {
            val inputRequest = currentCall.inputs.request(context, Inputs.Type.Camera.Internal)

            _camPermission.value = Permission(
                inputRequest is Inputs.RequestResult.Success,
                inputRequest is Inputs.RequestResult.Error.PermissionDenied.Forever
            )

            val input = inputRequest.getOrNull<Input.Video.Camera.Internal>() ?: return@launchWhenResumed

            currentCall.participants.value.me.streams.value.firstOrNull { it.id == CAMERA_STREAM_ID }?.video?.value = input

            input.state.filter { it is Input.State.Closed }.onEach {
                _camPermission.value = Permission(false, false)
            }.launchIn(this)
        }
    }

    override fun onAnswer() {
        currentCall.connect()
    }

    override fun onHangup() {
        currentCall.end()
    }

    override suspend fun onEnableCamera(context: FragmentActivity, enable: Boolean) {
        val input = currentCall.inputs.availableInputs.value.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull() ?: return
        if (enable) input.tryEnable() else input.tryDisable()
    }

    override suspend fun onEnableMic(context: FragmentActivity, enable: Boolean) {
        val input = currentCall.inputs.availableInputs.value.filterIsInstance<Input.Audio>().firstOrNull() ?: return
        if (enable) input.tryEnable() else input.tryDisable()
    }

    override fun onSwitchCamera() {
        val camera =
            currentCall.inputs.availableInputs.value.filterIsInstance<Input.Video.Camera.Internal>()
                .firstOrNull()
        val currentLens = camera?.currentLens?.value
        val newLens = camera?.lenses?.firstOrNull { it.isRear != currentLens?.isRear } ?: return
        camera.setLens(newLens)
    }

    override fun onSetVolume(value: Int) = callAudioManager.setVolume(value)

    override fun onSetZoom(value: Float) {
        val camera =
            currentCall.inputs.availableInputs.value.filterIsInstance<Input.Video.Camera.Internal>()
                .firstOrNull()
        val currentLens = camera?.currentLens?.value ?: return
        currentLens.zoom.value?.tryZoom(value)
    }
}