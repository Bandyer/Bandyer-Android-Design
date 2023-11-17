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

package com.kaleyra.video_common_ui.proximity

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.utils.CallExtensions.getMyInternalCamera
import com.kaleyra.video_common_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.video_common_ui.utils.CallExtensions.isMyInternalCameraUsingFrontLens

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