package com.kaleyra.collaboration_suite_core_ui.proximity

import android.app.Application
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner
import com.bandyer.android_audiosession.session.AudioCallSession
import com.kaleyra.collaboration_suite_core_ui.CallUI
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