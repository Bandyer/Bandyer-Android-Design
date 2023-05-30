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
import com.bandyer.android_audiosession.session.AudioCallSession
import com.bandyer.android_audiosession.session.AudioCallSessionInstance
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.getMyInternalCamera
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraUsingFrontLens
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsbInput
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsersWithCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isIncoming
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyScreenShareEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isNotConnected
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensor
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensorListener

internal class CallProximityDelegate<T>(private val lifecycleContext: T, private val call: CallUI): ProximitySensorListener where T: ContextWrapper, T: LifecycleOwner {

    private var proximityWakeLock: PowerManager.WakeLock? = null

    private var proximitySensor: ProximitySensor? = null

    private var callActivity: ProximityCallActivity? = null

    private val configuration = lifecycleContext.resources.configuration

    private val isDeviceLandscape: Boolean
        get() = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private var wasCameraEnabled = false

    private var wasLoudspeakerActive = false

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity !is ProximityCallActivity) return
            callActivity = activity
        }

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) = Unit

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            callActivity = null
        }
    }

    init {
        val powerManager = lifecycleContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, javaClass.simpleName)
        proximityWakeLock!!.setReferenceCounted(false)
    }

    fun bind() {
        getApplication()?.registerActivityLifecycleCallbacks(callbacks)
        proximitySensor = ProximitySensor.bind(lifecycleContext, this)
    }

    fun destroy() {
        getApplication()?.unregisterActivityLifecycleCallbacks(callbacks)
        proximitySensor?.destroy()
        proximitySensor = null
    }

    override fun onProximitySensorChanged(isNear: Boolean) = if (isNear) onProximityOn() else onProximityOff()

    private fun onProximityOn() {
        if (call.isIncoming()) return

        callActivity?.disableWindowTouch()

        val shouldAcquireProximityLock = shouldAcquireProximityLock()
        if (shouldAcquireProximityLock) {
            proximityWakeLock?.acquire(WakeLockTimeout)
        }

        wasCameraEnabled = call.isMyInternalCameraEnabled()
        val shouldDisableVideo = wasCameraEnabled && (shouldAcquireProximityLock || call.isMyInternalCameraUsingFrontLens())
        if (shouldDisableVideo) {
            call.getMyInternalCamera()?.tryDisable()
        }

        val audioCallSession = AudioCallSession.getInstance()
        wasLoudspeakerActive = audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Loudspeaker
        if (wasLoudspeakerActive) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Earpiece())
        }
    }

    private fun onProximityOff() {
        callActivity?.enableWindowTouch()

        proximityWakeLock?.release()

        if (wasCameraEnabled) {
            call.getMyInternalCamera()?.tryEnable()
        }

        val audioCallSession = AudioCallSession.getInstance()
        val shouldEnableLoudspeaker = wasLoudspeakerActive && audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Earpiece
        if (shouldEnableLoudspeaker) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Loudspeaker())
        }

        wasCameraEnabled = false
        wasLoudspeakerActive = false
    }

    private fun shouldAcquireProximityLock(): Boolean {
        val callActivity = callActivity
        return when {
            call.disableProximitySensor -> false
            callActivity == null -> false
            !callActivity.isInForeground -> false
            callActivity.isInPip -> false
            callActivity.isWhiteboardDisplayed -> false
            callActivity.isFileShareDisplayed -> false
            isDeviceLandscape && call.isNotConnected() && call.isMyInternalCameraEnabled() -> false
            isDeviceLandscape && call.hasUsersWithCameraEnabled() -> false
            call.isMyScreenShareEnabled() -> false
            call.hasUsbInput() -> false
            else -> true
        }
    }

    private fun AudioCallSessionInstance.tryEnableDevice(audioOutput: AudioOutputDevice) {
        val device = getAvailableAudioOutputDevices.firstOrNull { it.javaClass == audioOutput.javaClass }
        if (device != null) changeAudioOutputDevice(device)
    }

    private fun getApplication() = lifecycleContext.applicationContext as? Application

    companion object {
        private const val WakeLockTimeout = 60*60*1000L /*1 hour*/
    }
}