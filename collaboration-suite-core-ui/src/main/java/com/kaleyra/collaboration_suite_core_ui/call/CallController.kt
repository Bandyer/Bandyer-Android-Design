package com.kaleyra.collaboration_suite_core_ui.call

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_core_ui.model.Volume
import com.kaleyra.collaboration_suite_utils.audio.CallAudioManager

interface CallController: CallUIController {

    val callAudioManager: CallAudioManager

    val currentCall: Call

    override suspend fun onRequestMicPermission(context: FragmentActivity): Permission =
        if (currentCall.inputs.allowList.value.firstOrNull { it is Input.Audio } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall.inputs.request(context, Inputs.Type.Microphone)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }

    override suspend fun onRequestCameraPermission(context: FragmentActivity): Permission =
        if (currentCall.inputs.allowList.value.firstOrNull { it is Input.Video.Camera.Internal } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall.inputs.request(context, Inputs.Type.Camera.Internal)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }

    override fun onAnswer() { currentCall.connect() }

    override fun onHangup() { currentCall.end() }

    override fun onEnableCamera(enable: Boolean) {
        val video = currentCall.inputs.allowList.value.firstOrNull { it is Input.Video.Camera.Internal } ?: return
        if (enable) video.tryEnable() else video.tryDisable()
    }

    override fun onEnableMic(enable: Boolean) {
        val audio = currentCall.inputs.allowList.value.firstOrNull { it is Input.Audio } ?: return
        if (enable) audio.tryEnable() else audio.tryDisable()
    }

    override fun onSwitchCamera() {
        val camera = currentCall.inputs.allowList.value.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull()
        val currentLens = camera?.currentLens?.value
        val newLens = camera?.lenses?.firstOrNull { it.isRear != currentLens?.isRear } ?: return
        camera.setLens(newLens)
    }

    override fun onGetVolume(): Volume = Volume(
        callAudioManager.currentVolume,
        callAudioManager.minVolume,
        callAudioManager.maxVolume
    )

    override fun onSetVolume(value: Int) = callAudioManager.setVolume(value)

    override fun onSetZoom(value: Float) {
        val video = currentCall.inputs.allowList.value.firstOrNull { it is Input.Video.Camera.Internal } as Input.Video.Camera.Internal ?: return
        video.zoom.value = value
    }
}