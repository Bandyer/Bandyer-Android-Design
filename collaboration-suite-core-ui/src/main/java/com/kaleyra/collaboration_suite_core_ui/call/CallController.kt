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
            val permission = if (currentCall.inputs.allowList.value.firstOrNull { it is Input.Audio } != null) Permission(
                isAllowed = true,
                neverAskAgain = false
            )
            else currentCall.inputs.request(context, Inputs.Type.Microphone)
                .let {
                    Permission(
                        it is Inputs.RequestResult.Allow,
                        it is Inputs.RequestResult.Never
                    )
                }
            _micPermission.value = permission
        }
    }


    override fun onRequestCameraPermission(context: FragmentActivity) {
        context.lifecycleScope.launchWhenResumed {
            val permission = if (currentCall.inputs.allowList.value.firstOrNull { it is Input.Video.Camera.Internal } != null) Permission(
                isAllowed = true,
                neverAskAgain = false
            )
            else currentCall.inputs.request(context, Inputs.Type.Camera.Internal)
                .let {
                    Permission(
                        it is Inputs.RequestResult.Allow,
                        it is Inputs.RequestResult.Never
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

    override fun onEnableCamera(enable: Boolean) {
        val video =
            currentCall.inputs.allowList.value.firstOrNull { it is Input.Video.Camera.Internal }
                ?: return
        if (enable) video.tryEnable() else video.tryDisable()
    }

    override fun onEnableMic(enable: Boolean) {
        val audio = currentCall.inputs.allowList.value.firstOrNull { it is Input.Audio } ?: return
        if (enable) audio.tryEnable() else audio.tryDisable()
    }

    override fun onSwitchCamera() {
        val camera =
            currentCall.inputs.allowList.value.filterIsInstance<Input.Video.Camera.Internal>()
                .firstOrNull()
        val currentLens = camera?.currentLens?.value
        val newLens = camera?.lenses?.firstOrNull { it.isRear != currentLens?.isRear } ?: return
        camera.setLens(newLens)
    }

    override fun onSetVolume(value: Int) = callAudioManager.setVolume(value)

    override fun onSetZoom(value: Float) {
        val camera =
            currentCall.inputs.allowList.value.filterIsInstance<Input.Video.Camera.Internal>()
                .firstOrNull()
        val currentLens = camera?.currentLens?.value ?: return
        currentLens.zoom.value?.tryZoom(value)
    }
}