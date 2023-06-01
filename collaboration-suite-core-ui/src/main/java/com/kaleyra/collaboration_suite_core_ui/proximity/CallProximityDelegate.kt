package com.kaleyra.collaboration_suite_core_ui.proximity

import android.app.Application
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner
import com.bandyer.android_audiosession.session.AudioCallSession
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isIncoming
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensor
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensorListener

internal class CallProximityDelegate<T>(
    private val lifecycleContext: T,
    private val call: CallUI,
    private val wakeLockProximityDelegate: WakeLockProximityDelegate = WakeLockProximityDelegateImpl(lifecycleContext.applicationContext as Application, call),
    private val cameraProximityDelegate: CameraProximityDelegate = CameraProximityDelegateImpl(call),
    private val audioProximityDelegate: AudioProximityDelegate = AudioProximityDelegateImpl(AudioCallSession.getInstance())
) : ProximitySensorListener where T : ContextWrapper, T : LifecycleOwner {

    private var proximitySensor: ProximitySensor? = null

    fun bind() {
        proximitySensor = ProximitySensor.bind(lifecycleContext, this)
        wakeLockProximityDelegate.bind()
    }

    fun destroy() {
        wakeLockProximityDelegate.destroy()
        proximitySensor?.destroy()
        proximitySensor = null
    }

    override fun onProximitySensorChanged(isNear: Boolean) {
        if (call.isIncoming()) return
        if (isNear) {
            wakeLockProximityDelegate.tryTurnScreenOff()
            cameraProximityDelegate.tryDisableCamera(
                forceDisableCamera = wakeLockProximityDelegate.isScreenTurnedOff
            )
            audioProximityDelegate.trySwitchToEarpiece()
        } else {
            wakeLockProximityDelegate.restoreScreenOn()
            cameraProximityDelegate.restoreCamera()
            audioProximityDelegate.tryRestoreToLoudspeaker()
        }
    }
}