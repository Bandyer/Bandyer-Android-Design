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

import android.app.Application
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner
import com.bandyer.android_audiosession.session.AudioCallSession
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import com.kaleyra.video_utils.proximity_listener.ProximitySensorListener

internal class CallProximityDelegate<T>(
    private val lifecycleContext: T,
    private val call: CallUI,
    private val disableProximity: () -> Boolean,
    private val disableWindowTouch: (Boolean) -> Unit,
    private val wakeLockProximityDelegate: WakeLockProximityDelegate = WakeLockProximityDelegateImpl(lifecycleContext.applicationContext as Application, call),
    private val cameraProximityDelegate: CameraProximityDelegate = CameraProximityDelegateImpl(call),
    private val audioProximityDelegate: AudioProximityDelegate = AudioProximityDelegateImpl(AudioCallSession.getInstance())
) : ProximitySensorListener where T : ContextWrapper, T : LifecycleOwner {

    var sensor: ProximitySensor? = null
        private set

    fun bind() {
        sensor = ProximitySensor.bind(lifecycleContext, this)
        wakeLockProximityDelegate.bind()
    }

    fun destroy() {
        wakeLockProximityDelegate.destroy()
        sensor?.destroy()
        sensor = null
    }

    override fun onProximitySensorChanged(isNear: Boolean) {
        if (isNear) {
            if (!disableProximity()) {
                wakeLockProximityDelegate.tryTurnScreenOff()
                cameraProximityDelegate.tryDisableCamera(forceDisableCamera = wakeLockProximityDelegate.isScreenTurnedOff)
            }
            if (wakeLockProximityDelegate.isScreenTurnedOff) {
                disableWindowTouch(true)
            }
            audioProximityDelegate.trySwitchToEarpiece()
        } else {
            disableWindowTouch(false)
            wakeLockProximityDelegate.restoreScreenOn()
            cameraProximityDelegate.restoreCamera()
            audioProximityDelegate.tryRestoreToLoudspeaker()
        }
    }
}