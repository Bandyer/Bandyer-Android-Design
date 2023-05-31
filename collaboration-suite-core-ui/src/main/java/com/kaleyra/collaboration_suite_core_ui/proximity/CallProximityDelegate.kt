package com.kaleyra.collaboration_suite_core_ui.proximity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import android.os.PowerManager
import androidx.lifecycle.LifecycleOwner
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.session.AudioCallSessionInstance
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.getMyInternalCamera
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsbInput
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsersWithCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isIncoming
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraUsingFrontLens
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyScreenShareEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isNotConnected
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensor
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensorListener

interface AudioProximityDelegate {

    val audioCallSession: AudioCallSessionInstance

    fun trySwitchToEarpiece()

    fun tryRestoreToLoudspeaker()
}

class AudioProximityDelegateImpl(override val audioCallSession: AudioCallSessionInstance) : AudioProximityDelegate {

    private var wasLoudspeakerActive: Boolean = false

    override fun trySwitchToEarpiece() {
        wasLoudspeakerActive = audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Loudspeaker
        if (wasLoudspeakerActive) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Earpiece())
        }
    }

    override fun tryRestoreToLoudspeaker() {
        val shouldEnableLoudspeaker = wasLoudspeakerActive && audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Earpiece
        if (shouldEnableLoudspeaker) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Loudspeaker())
        }
        wasLoudspeakerActive = false
    }

    private fun AudioCallSessionInstance.tryEnableDevice(audioOutput: AudioOutputDevice) {
        val device = getAvailableAudioOutputDevices.firstOrNull { it.javaClass == audioOutput.javaClass }
        if (device != null) changeAudioOutputDevice(device)
    }
}

interface CameraProximityDelegate {
    val call: CallUI

    var forceDisableCamera: Boolean

    fun tryDisableCamera(forceDisableCamera: Boolean = false)

    fun restoreCamera()
}

class CameraProximityDelegateImpl(override val call: CallUI) : CameraProximityDelegate {

    private var wasCameraEnabled: Boolean = false

    override var forceDisableCamera = false

    override fun tryDisableCamera(forceDisableCamera: Boolean) {
        wasCameraEnabled = call.isMyInternalCameraEnabled()
        val shouldDisableVideo = wasCameraEnabled && (forceDisableCamera || call.isMyInternalCameraUsingFrontLens())
        if (shouldDisableVideo) {
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

interface WakeLockProximityDelegate {

    val application: Application

    val call: CallUI

    val isScreenTurnedOff: Boolean

    fun bind()

    fun destroy()

    fun tryTurnScreenOff()

    fun restoreScreenOn()
}

class WakeLockProximityDelegateImpl(
    override val application: Application,
    override val call: CallUI,
) : WakeLockProximityDelegate, Application.ActivityLifecycleCallbacks  {

    private var proximityWakeLock: PowerManager.WakeLock? = null

    private var proximityCallActivity: ProximityCallActivity? = null

    override var isScreenTurnedOff: Boolean = false
        private set

    private val isDeviceLandscape: Boolean
        get() = application.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    init {
        val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, javaClass.simpleName)
        proximityWakeLock!!.setReferenceCounted(false)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is ProximityCallActivity) return
        proximityCallActivity = activity
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        proximityCallActivity = null
    }

    override fun bind() {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun destroy() {
        application.unregisterActivityLifecycleCallbacks(this)
    }

    override fun tryTurnScreenOff() {
        val shouldAcquireProximityLock = shouldAcquireProximityLock()
        if (shouldAcquireProximityLock) {
            proximityWakeLock?.acquire(WakeLockTimeout)
        }
        isScreenTurnedOff = shouldAcquireProximityLock
    }

    override fun restoreScreenOn() {
        proximityWakeLock?.release()
        isScreenTurnedOff = false
    }

    private fun shouldAcquireProximityLock(): Boolean {
        return when {
            call.disableProximitySensor -> false
            proximityCallActivity?.enableProximity != true -> false
            isDeviceLandscape && call.isNotConnected() && call.isMyInternalCameraEnabled() -> false
            isDeviceLandscape && call.hasUsersWithCameraEnabled() -> false
            call.isMyScreenShareEnabled() -> false
            call.hasUsbInput() -> false
            else -> true
        }
    }

    companion object {
        private const val WakeLockTimeout = 60 * 60 * 1000L /*1 hour*/
    }
}

internal class CallProximityDelegate<T>(
    private val lifecycleContext: T,
    private val wakeLockProximityDelegate: WakeLockProximityDelegate,
    private val cameraProximityDelegate: CameraProximityDelegate,
    private val audioProximityDelegate: AudioProximityDelegate
) : ProximitySensorListener where T : ContextWrapper, T : LifecycleOwner {

    private var proximitySensor: ProximitySensor? = null

    private var callActivity: ProximityCallActivity? = null

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
        if (isNear) {
            if (call.isIncoming()) return
            callActivity?.disableWindowTouch()
            wakeLockProximityDelegate.tryTurnScreenOff()
            cameraProximityDelegate.tryDisableCamera(
                forceDisableCamera = wakeLockProximityDelegate.isScreenTurnedOff
            )
            audioProximityDelegate.trySwitchToEarpiece()
        } else {
            callActivity?.enableWindowTouch()
            wakeLockProximityDelegate.restoreScreenOn()
            cameraProximityDelegate.restoreCamera()
            audioProximityDelegate.tryRestoreToLoudspeaker()
        }
    }
}