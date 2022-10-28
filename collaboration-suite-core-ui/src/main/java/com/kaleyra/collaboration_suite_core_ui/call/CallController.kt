package com.kaleyra.collaboration_suite_core_ui.call

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_core_ui.model.Volume
import com.kaleyra.collaboration_suite_utils.audio.CallAudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
            val permission = currentCall.inputs.request(context, Inputs.Type.Microphone).let {
                Permission(
                    it is Inputs.RequestResult.Success,
                    it is Inputs.RequestResult.Error.PermissionDenied.Forever
                )
            }
            _micPermission.value = permission
        }
    }


    override fun onRequestCameraPermission(context: FragmentActivity) {
        context.lifecycleScope.launchWhenResumed {
            val permission = currentCall.inputs.request(context, Inputs.Type.Camera.Internal).let {
                Permission(
                    it is Inputs.RequestResult.Success,
                    it is Inputs.RequestResult.Error.PermissionDenied.Forever
                )
            }
            _camPermission.value = permission
        }
    }

    override fun onAnswer() {
        currentCall.connect()
    }

    override fun onHangup() {
        currentCall.end()
    }

    override suspend fun onEnableCamera(context: FragmentActivity, enable: Boolean) {
        val input: Input.Video.Camera.Internal = currentCall.inputs.request(context, Inputs.Type.Camera.Internal).getOrNull() ?: return
        currentCall.participants.value.me.streams.value.firstOrNull { it.id == CallStreamDelegate.MY_STREAM_ID }?.video?.value = input
        if (enable) input.tryEnable() else input.tryDisable()
    }

    override suspend fun onEnableMic(context: FragmentActivity, enable: Boolean) {
        val input: Input.Audio = currentCall.inputs.request(context, Inputs.Type.Microphone).getOrNull() ?: return
        currentCall.participants.value.me.streams.value.firstOrNull { it.id == CallStreamDelegate.MY_STREAM_ID }?.audio?.value = input
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