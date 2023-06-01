package com.kaleyra.collaboration_suite_core_ui.proximity

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.getMyInternalCamera
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraUsingFrontLens

interface CameraProximityDelegate {

    val call: CallUI

    var forceDisableCamera: Boolean

    fun tryDisableCamera(forceDisableCamera: Boolean = false)

    fun restoreCamera()
}

internal class CameraProximityDelegateImpl(override val call: CallUI) : CameraProximityDelegate {

    private var wasCameraEnabled: Boolean = false

    override var forceDisableCamera = false

    override fun tryDisableCamera(forceDisableCamera: Boolean) {
        wasCameraEnabled = call.isMyInternalCameraEnabled()
        val shouldDisableVideo = forceDisableCamera || call.isMyInternalCameraUsingFrontLens()
        if (wasCameraEnabled && shouldDisableVideo) {
            call.getMyInternalCamera()?.tryDisable()
        }
    }

    override fun restoreCamera() {
        if (wasCameraEnabled) {
            call.getMyInternalCamera()?.tryEnable()
        }
        wasCameraEnabled = false
    }

}